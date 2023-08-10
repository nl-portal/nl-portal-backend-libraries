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
package com.ritense.portal.zaak.web.rest

import com.ritense.portal.zaak.domain.documenten.Document
import java.util.UUID
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping(value = ["/api"])
interface DocumentContentResource {

    @GetMapping(value = ["/document/{documentId}/content"])
    fun downloadStreaming(@PathVariable("documentId") documentId: UUID): ResponseEntity<Flux<DataBuffer>>

    @PostMapping(value = ["/document/content"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    suspend fun uploadStreaming(
        @RequestPart("file") file: FilePart,
        @RequestPart("informatieobjecttype", required = false) informatieobjecttype: String?
    ): ResponseEntity<Document>
}