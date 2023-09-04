package com.ritense.portal.documentenapi.service

import com.ritense.portal.documentenapi.client.DocumentenApiClient
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.documentenapi.client.DocumentenApiConfig
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
                informatieobjecttype = documentenApiConfig.documentTypeUrl
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