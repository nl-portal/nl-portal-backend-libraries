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

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URLEncoder
import java.util.Base64
import nl.nlportal.core.qrcode.QRCodeFileExtension
import nl.nlportal.core.qrcode.QRCodeService
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.ShaVersion
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration.DirectPaymentProperties
import nl.nlportal.payment.direct.domain.DirectPaymentRequest
import nl.nlportal.payment.direct.domain.DirectPaymentResponse
import nl.nlportal.payment.direct.domain.DirectPaymentStatus
import nl.nlportal.payment.direct.service.DirectPaymentService
import nl.nlportal.payment.qrlink.autoconfiguratie.QRLinkPaymentModuleConfiguration.QRLinkPaymentProperties
import nl.nlportal.payment.qrlink.domain.QRLinkPaymentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class QRLinkPaymentService(
    private val directPaymentService: DirectPaymentService,
    private val qrCodeService: QRCodeService,
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
        qrCodeFileType: QRCodeFileExtension? = null,
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
        return QRLinkPaymentResponse(
            qrcode = Base64.getEncoder().encodeToString(qrcodeFile),
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
        qrCodeFileType: QRCodeFileExtension? = null,
        subject: String? = null,
        margin: Int? = 0,
    ): ByteArray {
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

            return qrCodeService.generateQrCode(
                link = link,
                filePrefix = "qrcode-$orderid",
                height = height ?: qrLinkPaymentProperties.qrcodeHeight,
                width = width ?: qrLinkPaymentProperties.qrcodeWidth,
                extension = qrCodeFileType,
                margin = margin ?: 0
            )
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
        hash: String,
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
