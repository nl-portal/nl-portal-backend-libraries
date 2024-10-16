package nl.nlportal.besluiten.service

import nl.nlportal.besluiten.client.BesluitenApiClient
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import java.util.UUID

class BesluitenService(
    val besluitenApiClient: BesluitenApiClient,
) {
    suspend fun getBesluiten(
        besluitType: String? = null,
        identificatie: String? = null,
        page: Int? = 1,
        verantwoordelijkeOrganisatie: String? = null,
        zaak: String? = null,
    ): List<Besluit> {
        return besluitenApiClient.getBesluiten(
            besluitType = besluitType,
            identificatie = identificatie,
            page = page,
            verantwoordelijkeOrganisatie = verantwoordelijkeOrganisatie,
            zaak = zaak,
        )
    }

    suspend fun getBesluit(besluitId: UUID): Besluit {
        return besluitenApiClient.getBesluit(besluitId)
    }

    suspend fun getBesluitAuditTrails(besluitId: UUID): List<BesluitAuditTrail> {
        return besluitenApiClient.getBesluitAuditTrails(besluitId).sortedBy { it.aanmaakdatum }
    }

    suspend fun getBesluitAuditTrail(
        besluitId: UUID,
        auditTrailId: UUID,
    ): BesluitAuditTrail {
        return besluitenApiClient.getBesluitAuditTrail(besluitId, auditTrailId)
    }

    suspend fun getBesluitDocumenten(
        besluit: String? = null,
        informatieobject: String? = null,
    ): List<BesluitDocument> {
        return besluitenApiClient.getBesluitDocumenten(besluit, informatieobject)
    }

    suspend fun getBesluitDocument(documentId: UUID): BesluitDocument {
        return besluitenApiClient.getBesluitDocument(documentId)
    }
}