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
package nl.nlportal.documentenapi.web.rest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.documentenapi.client.DocumentenApiClient
import nl.nlportal.documentenapi.domain.VirusScanStatus
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.documentenapi.service.VirusScanService
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(value = ["/api"])
class DocumentContentResource(
    val documentenApiClient: DocumentenApiClient,
    val documentenApiService: DocumentenApiService,
    val virusScanService: VirusScanService?,
    val documentApisConfig: DocumentApisConfig,
) {
    @GetMapping(value = ["/documentapi/{documentapi}/document/{documentId}/content"])
    fun downloadStreaming(
        @PathVariable documentId: UUID,
        @PathVariable documentapi: String,
    ): ResponseEntity<Flow<DataBuffer>> {
        // Request service to get the file's data stream
        val fileDataStream = documentenApiService.getDocumentContentStreaming(documentId, documentapi)

        val document = runBlocking { documentenApiService.getDocument(documentId, documentapi) }

        val responseHeaders =
            HttpHeaders().apply {
                set("Content-Disposition", "attachment; filename=\"${document.bestandsnaam}\"")
            }

        return ResponseEntity.ok().headers(responseHeaders).contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(fileDataStream)
    }

    @PostMapping(
        value = ["/documentapi/{documentapi}/document/content"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    suspend fun uploadStreaming(
        @RequestPart("file") file: FilePart,
        @PathVariable documentapi: String,
    ): ResponseEntity<Any> {
        val virusScanResult = virusScanService?.scan(file.content())

        // only return a bad request as a virus is found, otherwise continue....
        if (VirusScanStatus.VIRUS_FOUND == virusScanResult?.status) {
            return ResponseEntity(virusScanResult, HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity.ok(documentenApiService.uploadDocument(file, documentapi))
    }

    @PostMapping(value = ["/document/content"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadStreamingDefault(
        @RequestPart("file") file: FilePart,
    ): ResponseEntity<Any> {
        val documentapi: String = documentApisConfig.defaultDocumentApi
        val virusScanResult = virusScanService?.scan(file.content())

        // only return a bad request as a virus is found, otherwise continue....
        if (VirusScanStatus.VIRUS_FOUND == virusScanResult?.status) {
            return ResponseEntity(virusScanResult, HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity.ok(documentenApiService.uploadDocument(file, documentapi))
    }
}