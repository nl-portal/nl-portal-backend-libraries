package nl.nlportal.besluiten.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import nl.nlportal.besluiten.service.BesluitenService
import java.util.UUID

class BesluitenQuery(
    val besluitenService: BesluitenService,
) : Query {
    @GraphQLDescription("Get all besluiten")
    suspend fun getBesluiten(
        besluitType: String? = null,
        identificatie: String? = null,
        pageNumber: Int? = 1,
        verantwoordelijkeOrganisatie: String? = null,
        zaak: String? = null,
    ): BesluitPage {
        val besluiten =
            besluitenService.getBesluiten(
                besluitType = besluitType,
                identificatie = identificatie,
                page = pageNumber,
                verantwoordelijkeOrganisatie = verantwoordelijkeOrganisatie,
                zaak = zaak,
            )

        return BesluitPage.fromList(pageNumber, besluiten)
    }

    @GraphQLDescription("Get all besluit by id")
    suspend fun getBesluit(besluitId: UUID): Besluit {
        return besluitenService.getBesluit(besluitId)
    }

    @GraphQLDescription("Get all besluit audit trails")
    suspend fun getBesluitAuditTrails(besluitId: UUID): List<BesluitAuditTrail> {
        return besluitenService.getBesluitAuditTrails(besluitId)
    }

    @GraphQLDescription("Get all besluit audit trails by id")
    suspend fun getBesluitAuditTrail(
        besluitId: UUID,
        auditTrail: UUID,
    ): BesluitAuditTrail {
        return besluitenService.getBesluitAuditTrail(besluitId, auditTrail)
    }

    @GraphQLDescription("Get all besluit documents")
    suspend fun getBesluitDocumenten(
        besluit: String? = null,
        informatieobject: String? = null,
    ): List<BesluitDocument> {
        return besluitenService.getBesluitDocumenten(besluit, informatieobject)
    }

    @GraphQLDescription("Get all besluit document by id")
    suspend fun getBesluitDocument(documentId: UUID): BesluitDocument {
        return besluitenService.getBesluitDocument(documentId)
    }
}