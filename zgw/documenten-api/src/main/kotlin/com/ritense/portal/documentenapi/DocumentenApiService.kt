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
package com.ritense.portal.documentenapi

import com.ritense.portal.catalogiapi.client.DocumentenApiClient
import com.ritense.portal.catalogiapi.client.DocumentenApiConfig
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.documentenapi.domain.Document
import com.ritense.portal.documentenapi.domain.DocumentContent
import com.ritense.portal.documentenapi.domain.DocumentStatus
import com.ritense.portal.documentenapi.domain.PostEnkelvoudiginformatieobjectRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.context.ReactiveSecurityContextHolder

class DocumentenApiService(
    val documentenApiClient: DocumentenApiClient,
    val documentenApiConfig: DocumentenApiConfig
) {

    suspend fun getDocument(documentId: UUID): Document {
        return documentenApiClient.getDocument(documentId)
    }

    suspend fun getDocument(documentUrl: String): Document {
        return documentenApiClient.getDocument(extractId(documentUrl))
    }

    suspend fun getDocumentContent(documentId: UUID): DocumentContent {
        val documentContent = documentenApiClient.getDocumentContent(documentId)
        return DocumentContent(Base64.getEncoder().encodeToString(documentContent))
    }

    suspend fun uploadDocument(file: FilePart): Document {
        val auteur = ReactiveSecurityContextHolder.getContext()
            .map { (it.authentication as CommonGroundAuthentication).getUserId() }
            .awaitSingleOrNull() ?: "valtimo"

        return documentenApiClient.postDocument(
            PostEnkelvoudiginformatieobjectRequest(
                bronorganisatie = documentenApiConfig.rsin,
                creatiedatum = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                titel = file.filename(),
                auteur = auteur,
                status = DocumentStatus.DEFINITIEF,
                taal = "nld",
                bestandsnaam = file.filename(),
                indicatieGebruiksrecht = false,
                informatieobjecttype = documentenApiConfig.documentTypeUrl,
            ),
            file.content()
        )
    }

    companion object {
        fun extractId(url: String): UUID {
            return UUID.fromString(url.substringAfterLast("/"))
        }
    }
}