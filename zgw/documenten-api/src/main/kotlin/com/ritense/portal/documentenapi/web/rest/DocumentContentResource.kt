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
package com.ritense.portal.documentenapi.web.rest

import com.ritense.portal.documentenapi.client.DocumentenApiClient
import com.ritense.portal.documentenapi.client.DocumentenApiVirusScanConfig
import com.ritense.portal.documentenapi.domain.VirusScanResult
import com.ritense.portal.documentenapi.service.DocumentenApiService
import com.ritense.portal.documentenapi.service.VirusScanService
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*


@RestController
@RequestMapping(value = ["/api"])
class DocumentContentResource(
    val documentenApiClient: DocumentenApiClient,
    val documentenApiService: DocumentenApiService,
    val virusScanService: VirusScanService,
    val documentenApiVirusScanConfig: DocumentenApiVirusScanConfig
) {

    @GetMapping(value = ["/document/{documentId}/content"])
    fun downloadStreaming(@PathVariable documentId: UUID): ResponseEntity<Flux<DataBuffer>> {
        // Request service to get the file's data stream
        val fileDataStream = documentenApiClient.getDocumentContentStream(documentId)

        val document = runBlocking { documentenApiService.getDocument(documentId) }

        val responseHeaders = HttpHeaders().apply {
            set("Content-Disposition", "attachment; filename=\"${document.bestandsnaam}\"")
        }

        return ResponseEntity
            .ok()
            .headers(responseHeaders)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(fileDataStream)
    }

    @PostMapping(value = ["/document/content"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadStreaming(@RequestPart("file") file: FilePart): ResponseEntity<Any> {
        if(documentenApiVirusScanConfig.enabled){
            //virus scan is enabled
            val virusScanResult = virusScanService.scan(dataBufferToInputStream(file.content()))

            // only return a bad request as a virus is found, otherwise continue....
            if(VirusScanResult.OK != virusScanResult) {
                return ResponseEntity(virusScanResult, HttpStatus.BAD_REQUEST)
            }
        }
        return ResponseEntity.ok(documentenApiService.uploadDocument(file))
    }

    fun dataBufferToInputStream(content: Flux<DataBuffer>): InputStream {
        val osPipe = PipedOutputStream()
        val isPipe = PipedInputStream(osPipe)
        DataBufferUtils.write(content, osPipe)
                .subscribe(DataBufferUtils.releaseConsumer());

        return isPipe
    }
}
