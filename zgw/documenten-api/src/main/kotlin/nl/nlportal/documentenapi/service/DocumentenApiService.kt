package nl.nlportal.documentenapi.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import nl.nlportal.core.util.CoreUtils.extractId
import nl.nlportal.documentenapi.client.DocumentApisConfig.DocumentenApisConfigProperties
import nl.nlportal.documentenapi.client.DocumentenApiClient
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.domain.DocumentStatus
import nl.nlportal.documentenapi.domain.PostEnkelvoudiginformatieobjectRequest
import nl.nlportal.documentenapi.exceptions.MimeTypeDeniedException
import nl.nlportal.portal.authentication.domain.PortalAuthentication
import org.apache.tika.Tika
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.security.core.context.ReactiveSecurityContextHolder

class DocumentenApiService(
    val documentenApiClient: DocumentenApiClient,
    val documentenApisConfigProperties: DocumentenApisConfigProperties,
) {
    suspend fun getDocument(
        documentId: UUID,
        documentApi: String,
    ): Document {
        return documentenApiClient.getDocument(documentId, documentApi)
    }

    suspend fun getDocument(documentUrl: String): Document {
        return documentenApiClient.getDocument(
            extractId(documentUrl),
            documentenApisConfigProperties.getConfigForDocumentUrl(documentUrl),
        )
    }

    fun getDocumentContentStreaming(
        documentId: UUID,
        documentApi: String,
    ): Flow<DataBuffer> {
        return documentenApiClient.getDocumentContentStream(documentId, documentApi)
    }

    fun getDocumentContentStreaming(informatieobejctUrl: String): Flow<DataBuffer> {
        val informatieObjectId = extractId(informatieobejctUrl)
        val documentenApi = documentenApisConfigProperties.getConfigForDocumentUrl(informatieobejctUrl)

        return documentenApiClient.getDocumentContentStream(informatieObjectId, documentenApi)
    }

    suspend fun uploadDocument(
        content: ByteArray,
        filename: String,
        documentApi: String,
        informatieobjecttype: String? = null,
    ): Document {
        val auteur =
            ReactiveSecurityContextHolder.getContext()
                .map { (it.authentication as PortalAuthentication).userId }
                .awaitSingleOrNull() ?: "valtimo"
        val documentenApiConfig = documentenApisConfigProperties.getConfig(documentApi)

        if (documentenApisConfigProperties.allowedMimeTypes.isNotEmpty()) {
            val mediaType = Tika().detect(content).split(";")[0].trim()
            if (mediaType !in documentenApisConfigProperties.allowedMimeTypes) {
                throw MimeTypeDeniedException("$mediaType is not allowed for uploads.")
            }
        }

        return documentenApiClient.postDocument(
            PostEnkelvoudiginformatieobjectRequest(
                bronorganisatie = documentenApiConfig.rsin!!,
                creatiedatum = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                titel = filename,
                auteur = auteur,
                status = DocumentStatus.DEFINITIEF,
                taal = "nld",
                bestandsnaam = filename,
                indicatieGebruiksrecht = false,
                informatieobjecttype =
                    informatieobjecttype
                        .takeUnless {
                            it.isNullOrEmpty()
                        }
                        ?: documentenApiConfig.documentTypeUrl!!,
            ),
            content,
            documentApi,
        )
    }

    fun filterDocuments(
        documents: List<Document>,
    ): List<Document> {
        return documents.filter { document ->
            document.status in documentenApisConfigProperties.statusWhitelist &&
                    document.vertrouwelijkheidaanduiding in documentenApisConfigProperties.vertrouwelijkheidsaanduidingWhitelist
        }
    }
}
