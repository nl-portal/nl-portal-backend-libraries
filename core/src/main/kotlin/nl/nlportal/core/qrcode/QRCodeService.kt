/*
 * Copyright 2026 Ritense BV, the Netherlands.
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
package nl.nlportal.core.qrcode

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.util.EnumMap
import org.apache.commons.io.FileUtils

class QRCodeService {
    fun generateQrCode(
        filePrefix: String,
        link: String,
        height: Int,
        width: Int,
        extension: QRCodeFileExtension? = null,
        margin: Int? = 0,
    ): ByteArray {
        try {
            val qrCodeFileExtensionExtension =
                when {
                    extension != null -> {
                        extension.toString()
                    }

                    else -> {
                        QRCodeFileExtension.PNG.toString()
                    }
                }

            val qrcodeFile = File.createTempFile(filePrefix, ".$qrCodeFileExtensionExtension")

            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = margin ?: 0
            if (margin == null) {
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            }

            val matrix =
                QRCodeWriter().encode(
                    link,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints,
                )

            FileOutputStream(qrcodeFile).use { out ->
                MatrixToImageWriter.writeToStream(matrix, qrCodeFileExtensionExtension, out, MatrixToImageConfig())
            }

            return FileUtils.readFileToByteArray(qrcodeFile)
        } catch (e: Exception) {
            logger.error { "Could not generate qrcode for link $link: ${e.message}" }
            throw QRCodeException("Could not generate qrcode for link $link", e)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}