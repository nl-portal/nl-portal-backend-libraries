/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.nlportal.payment.qrlink.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.Base64
import java.util.EnumMap
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.ShaVersion
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration.DirectPaymentProperties
import nl.nlportal.payment.direct.domain.DirectPaymentRequest
import nl.nlportal.payment.direct.domain.DirectPaymentResponse
import nl.nlportal.payment.direct.domain.DirectPaymentStatus
import nl.nlportal.payment.direct.service.DirectPaymentService
import nl.nlportal.payment.qrlink.autoconfiguratie.QRLinkPaymentModuleConfiguration.QRLinkPaymentProperties
import nl.nlportal.payment.qrlink.domain.QRLinkPaymentCodeFileType
import nl.nlportal.payment.qrlink.domain.QRLinkPaymentResponse
import org.apache.commons.io.FileUtils
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class QRLinkPaymentService(
    private val directPaymentService: DirectPaymentService,
    private val qrLinkPaymentProperties: QRLinkPaymentProperties,
    private val directPaymentProperties: DirectPaymentProperties,
) {
    fun generateLink(
        identifier: String,
        amountInCents: Int,
        orderid: String,
        reference: String,
        returnUrl: String? = null,
        subject: String? = null,
    ): String {
        val listNoValidParameters =
            validateRequiredParameters(
                identifier = identifier,
                orderid = orderid,
                reference = reference,
            )
        if (listNoValidParameters.isNotEmpty()) {
            val errorMessage = "Parameters '${listNoValidParameters.joinToString()}' are required"
            logger.error { errorMessage }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
        }
        if (!isIdentifierConfigured(
                identifier = identifier,
            )
        ) {
            logger.error { "Generate betaal link for $identifier is not configured" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$identifier is not configured")
        }

        val amount = amountInCents.toDouble() / 100
        val hash =
            generateHash(
                identifier = identifier,
                amount = amount,
                orderid = orderid,
                reference = reference,
            )

        var betaalLink = qrLinkPaymentProperties.landingUrl + "?identifier=$identifier&amount=$amount&orderid=$orderid&reference=$reference&hash=$hash"
        returnUrl?.let {
            betaalLink = "$betaalLink&returnurl=$it"
        }

        subject?.let {
            betaalLink = "$betaalLink&subject=" + URLEncoder.encode(it, "UTF-8").replace("+", "%20")
        }

        logger.debug { "Generate betaal link: $identifier with $betaalLink" }
        return betaalLink
    }

    fun generateQrCode(
        identifier: String,
        amountInCents: Int,
        orderid: String,
        reference: String,
        height: Int? = null,
        width: Int? = null,
        returnUrl: String? = null,
        qrCodeFileType: QRLinkPaymentCodeFileType? = null,
        subject: String? = null,
        margin: Int? = 0,
    ): QRLinkPaymentResponse {
        val qrcodeFile =
            generateQrCodeFile(
                identifier = identifier,
                amountInCents = amountInCents,
                orderid = orderid,
                reference = reference,
                returnUrl = returnUrl,
                height = height,
                width = width,
                qrCodeFileType = qrCodeFileType,
                subject = subject,
                margin = margin,
            )

        val fileContent = FileUtils.readFileToByteArray(qrcodeFile)
        return QRLinkPaymentResponse(
            qrcode = Base64.getEncoder().encodeToString(fileContent),
        )
    }

    fun generateQrCodeFile(
        identifier: String,
        amountInCents: Int,
        orderid: String,
        reference: String,
        height: Int? = null,
        width: Int? = null,
        returnUrl: String? = null,
        qrCodeFileType: QRLinkPaymentCodeFileType? = null,
        subject: String? = null,
        margin: Int? = 0,
    ): File {
        try {
            val link =
                generateLink(
                    identifier = identifier,
                    amountInCents = amountInCents,
                    orderid = orderid,
                    reference = reference,
                    returnUrl = returnUrl,
                    subject = subject,
                )
            val qrCodeFileTypeExtension =
                when {
                    qrCodeFileType != null -> {
                        qrCodeFileType.toString()
                    }

                    else -> {
                        QRLinkPaymentCodeFileType.PNG.toString()
                    }
                }
            val qrcodeFile = File.createTempFile("qrcode-$orderid", ".$qrCodeFileTypeExtension")

            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = margin ?: 0
            if (margin == null) {
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            }

            val matrix =
                QRCodeWriter().encode(
                    link,
                    BarcodeFormat.QR_CODE,
                    width ?: qrLinkPaymentProperties.qrcodeWidth,
                    height ?: qrLinkPaymentProperties.qrcodeHeight,
                    hints,
                )

            FileOutputStream(qrcodeFile).use { out ->
                MatrixToImageWriter.writeToStream(matrix, qrCodeFileTypeExtension, out, MatrixToImageConfig())
            }

            return qrcodeFile
        } catch (ex: ResponseStatusException) {
            throw ex
        } catch (e: Exception) {
            logger.error { "Could not generate qrcode with $identifier for $orderid: ${e.message}" }
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating qrcode file for $identifier and order id $orderid", e)
        }
    }

    suspend fun doBetaling(
        identifier: String,
        amount: Double,
        orderid: String,
        reference: String,
        language: String? = null,
        returnUrl: String? = null,
        hash: String? = null,
    ): DirectPaymentResponse {
        val newHash =
            generateHash(
                identifier = identifier,
                amount = amount,
                orderid = orderid,
                reference = reference,
            )

        if (hash != newHash) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment data has been tempered")
        }
        val paymentRequest =
            DirectPaymentRequest(
                reference = reference,
                amount = amount,
                orderId = orderid,
                identifier = identifier,
                langId = language,
                returnUrl = returnUrl ?: qrLinkPaymentProperties.returnUrl,
            )
        return directPaymentService.doDirectPayment(
            paymentRequest = paymentRequest,
        )
    }

    suspend fun getStatus(
        identifier: String,
        hostedCheckoutId: String,
    ): DirectPaymentStatus =
        directPaymentService.getDirectPaymentStatus(
            identifier = identifier,
            hostedCheckoutId = hostedCheckoutId,
        )

    fun isIdentifierConfigured(identifier: String): Boolean {
        val paymentDirectProfile =
            directPaymentProperties.getPaymentProfile(identifier)
                ?: directPaymentProperties.getPaymentProfileByPspPid(identifier)

        return paymentDirectProfile != null
    }

    fun generateHash(
        identifier: String,
        amount: Double,
        orderid: String,
        reference: String,
    ): String {
        val concatenateProperties = qrLinkPaymentProperties.secret + identifier + amount.toString() + orderid + reference
        return CoreUtils.createHash(concatenateProperties, ShaVersion.SHA1.version)
    }

    fun validateRequiredParameters(
        identifier: String,
        orderid: String,
        reference: String,
    ): List<String> {
        val listNoValidParameters = mutableListOf<String>()
        if (identifier.isBlank()) {
            listNoValidParameters.add("identifier")
        }

        if (reference.isBlank()) {
            listNoValidParameters.add("reference")
        }

        if (orderid.isBlank()) {
            listNoValidParameters.add("orderid")
        }

        return listNoValidParameters
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}
