/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.zaak.web.rest.impl

import com.ritense.portal.zaak.client.OpenZaakClient
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.service.ZaakService
import com.ritense.portal.zaak.web.rest.DocumentContentResource
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux

class DocumentContentResource(
    val openZaakClient: OpenZaakClient,
    val zaakService: ZaakService
) : DocumentContentResource {

    override fun downloadStreaming(documentId: UUID): ResponseEntity<Flux<DataBuffer>> {
        // Request service to get the file's data stream
        val fileDataStream = openZaakClient.getDocumentContentStream(documentId)

        val document = runBlocking { zaakService.getDocument(documentId) }

        val responseHeaders = HttpHeaders()
        responseHeaders.set("Content-Disposition", "attachment; filename=\"${document.bestandsnaam}\"")

        return ResponseEntity
            .ok()
            .headers(responseHeaders)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(fileDataStream)
    }

    override suspend fun uploadStreaming(file: FilePart): ResponseEntity<Document> {
        return ResponseEntity.ok(zaakService.uploadDocument(file))
    }
}