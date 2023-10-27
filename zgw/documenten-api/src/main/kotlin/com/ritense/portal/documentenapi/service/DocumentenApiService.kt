package com.ritense.portal.documentenapi.service

import com.ritense.portal.documentenapi.client.DocumentenApiClient
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.documentenapi.client.DocumentApisConfig
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
    val documentenApiConfig: DocumentApisConfig,
) {

    suspend fun getDocument(documentId: UUID, documentApi: String): Document {
        return documentenApiClient.getDocument(documentId, documentApi)
    }

    suspend fun getDocument(documentUrl: String): Document {
        return documentenApiClient.getDocument(extractId(documentUrl), documentenApiConfig.getConfigForDocumentUrl(documentUrl))
    }

    suspend fun getDocumentContent(documentId: UUID, documentApi: String): DocumentContent {
        val documentContent = documentenApiClient.getDocumentContent(documentId, documentApi)
        return DocumentContent(Base64.getEncoder().encodeToString(documentContent))
    }

    suspend fun uploadDocument(file: FilePart, documentApi: String): Document {
        val auteur = ReactiveSecurityContextHolder.getContext()
            .map { (it.authentication as CommonGroundAuthentication).getUserId() }
            .awaitSingleOrNull() ?: "valtimo"

        return documentenApiClient.postDocument(
            PostEnkelvoudiginformatieobjectRequest(
                bronorganisatie = documentenApiConfig.getConfig(documentApi).rsin!!,
                creatiedatum = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                titel = file.filename(),
                auteur = auteur,
                status = DocumentStatus.DEFINITIEF,
                taal = "nld",
                bestandsnaam = file.filename(),
                indicatieGebruiksrecht = false,
                informatieobjecttype = documentenApiConfig.getConfig(documentApi).documentTypeUrl!!,
            ),
            file.content(),
            documentApi,
        )
    }

    companion object {
        fun extractId(url: String): UUID {
            return UUID.fromString(url.substringAfterLast("/"))
        }
    }
}