/*
 * Copyright (c) 2026 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.web.rest

import kotlinx.coroutines.flow.Flow
import nl.nlportal.berichten.service.BerichtenService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.documentenapi.util.FilenameSanitizer
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/berichten"])
class BerichtDocumentResource(
    private val berichtenService: BerichtenService,
) {
    @GetMapping(value = ["/{berichtId}/document/{documentId}/content"])
    suspend fun downloadStreaming(
        @PathVariable berichtId: UUID,
        @PathVariable documentId: UUID,
        authentication: CommonGroundAuthentication,
    ): ResponseEntity<Flow<DataBuffer>> {
        val (document, content) =
            berichtenService.getBerichtDocumentContent(authentication, berichtId, documentId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Bericht not found")

        return ResponseEntity
            .ok()
            .headers(
                HttpHeaders().apply {
                    set(
                        "Content-Disposition",
                        FilenameSanitizer.encodeForContentDisposition(document.bestandsnaam),
                    )
                },
            ).contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(content)
    }
}