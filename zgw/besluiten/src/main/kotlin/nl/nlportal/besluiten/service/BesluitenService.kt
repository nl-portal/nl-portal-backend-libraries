package nl.nlportal.besluiten.service

import nl.nlportal.besluiten.client.BesluitenApiClient
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils.extractId
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.service.DocumentenApiService
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class BesluitenService(
    private val besluitenApiClient: BesluitenApiClient,
    private val documentenApiService: DocumentenApiService,
) {
    suspend fun getBesluiten(
        besluitType: String? = null,
        identificatie: String? = null,
        page: Int? = 1,
        verantwoordelijkeOrganisatie: String? = null,
        zaak: String? = null,
    ): List<Besluit> {

        val besluiten = besluitenApiClient.getBesluiten(
            besluitType = besluitType,
            identificatie = identificatie,
            page = page,
            verantwoordelijkeOrganisatie = verantwoordelijkeOrganisatie,
            zaak = zaak,
        )

        return besluiten
    }

    suspend fun getBesluitAuditTrails(besluitId: UUID): List<BesluitAuditTrail> {
        return besluitenApiClient.getBesluitAuditTrails(besluitId).sortedBy { it.aanmaakdatum }
    }

    suspend fun getBesluitDocumenten(
        authentication: CommonGroundAuthentication,
        besluit: String,
        informatieobject: String? = null,
    ): List<Document> {
        val besluitDocuments = besluitenApiClient.getBesluitDocumenten(
            besluit = besluit,
            informatieobject = informatieobject
        )
        return documentenApiService.filterDocuments(
            besluitDocuments.map {
                documentenApiService
                    .getDocument(it.informatieobject)
                    .copy(identificatie = authentication.userId)
            },
        )
    }

    suspend fun getBerichtDocumentContent(
        authentication: CommonGroundAuthentication,
        besluitId: UUID,
        documentId: UUID,
    ): Pair<Document, Flow<DataBuffer>>? {
        val besluit = besluitenApiClient.getBesluit(
            besluitId = besluitId,
        )
        val besluitDocument = besluitenApiClient.getBesluitDocument(documentId)
        if(besluitDocument.besluit != besluit.url) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Besluit document is not related to besluit")
        }

        val document = documentenApiService.getDocument(besluitDocument.url)
        val content = documentenApiService.getDocumentContentStreaming(besluitDocument.url)
        return document to content
    }

    suspend fun getBesluitDocument(documentId: UUID): BesluitDocument {
        return besluitenApiClient.getBesluitDocument(documentId)
    }
}
