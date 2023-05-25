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
import com.ritense.portal.documentenapi.domain.Document
import com.ritense.portal.documentenapi.service.DocumentenApiService
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux

class DocumentContentResource(
    val documentenApiClient: DocumentenApiClient,
    val documentenApiService: DocumentenApiService
) {

    fun downloadStreaming(documentId: UUID): ResponseEntity<Flux<DataBuffer>> {
        // Request service to get the file's data stream
        val fileDataStream = documentenApiClient.getDocumentContentStream(documentId)

        val document = runBlocking { documentenApiService.getDocument(documentId) }

        val responseHeaders = HttpHeaders()
        responseHeaders.set("Content-Disposition", "attachment; filename=\"${document.bestandsnaam}\"")

        return ResponseEntity
            .ok()
            .headers(responseHeaders)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(fileDataStream)
    }

    suspend fun uploadStreaming(file: FilePart): ResponseEntity<Document> {
        return ResponseEntity.ok(documentenApiService.uploadDocument(file))
    }
}