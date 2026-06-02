/*
 * Copyright 2015-2026 Ritense BV, the Netherlands.
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
package nl.nlportal.zgw.taak.web.rest

import kotlinx.coroutines.reactive.awaitSingle
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.documentenapi.domain.VirusScanStatus
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.documentenapi.service.VirusScanService
import nl.nlportal.documentenapi.util.FilenameSanitizer
import nl.nlportal.zgw.taak.service.TaakService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@ConditionalOnProperty(prefix = "nl-portal.config.documentenapis", name = ["enabled"], havingValue = "true")
@RequestMapping(value = ["/api/taak"])
class TaakDocumentUploadResource(
    private val taakService: TaakService,
    private val documentenApiService: DocumentenApiService,
    private val virusScanService: VirusScanService?,
    private val documentApisConfig: DocumentApisConfig,
) {
    @PostMapping(
        value = ["/{taakId}/document/content"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    suspend fun uploadDocument(
        @PathVariable taakId: UUID,
        @RequestPart("file") file: FilePart,
        @RequestPart("informatieobjecttype", required = false) informatieobjecttype: String?,
        authentication: CommonGroundAuthentication,
    ): ResponseEntity<Any> {
        taakService.getTaakByIdV2(taakId, authentication)

        val sanitizedFilename = FilenameSanitizer.sanitize(file.filename())
        val bufferedContent = bufferFileContent(file)

        val virusScanResult = virusScanService?.scan(bufferedContent)
        if (virusScanResult?.status == VirusScanStatus.VIRUS_FOUND) {
            return ResponseEntity(virusScanResult, HttpStatus.BAD_REQUEST)
        }

        val documentApi = documentApisConfig.properties.defaultDocumentApi
        return try {
            ResponseEntity.ok(
                documentenApiService.uploadDocument(
                    bufferedContent,
                    sanitizedFilename,
                    documentApi,
                    informatieobjecttype,
                ),
            )
        } catch (e: Exception) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    private suspend fun bufferFileContent(file: FilePart): ByteArray {
        val dataBuffer = DataBufferUtils.join(file.content()).awaitSingle()
        return try {
            val bytes = ByteArray(dataBuffer.readableByteCount())
            dataBuffer.read(bytes)
            bytes
        } finally {
            DataBufferUtils.release(dataBuffer)
        }
    }
}
