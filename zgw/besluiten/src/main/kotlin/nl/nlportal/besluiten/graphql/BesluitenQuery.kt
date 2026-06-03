package nl.nlportal.besluiten.graphql

import java.util.UUID
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.catalogiapi.domain.BesluitType
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BesluitenQuery(
    val besluitenService: BesluitenService,
    val catalogiApiService: CatalogiApiService
) {
    @QueryMapping
    suspend fun getBesluiten(
        authentication: CommonGroundAuthentication,
        @Argument besluitType: String? = null,
        @Argument identificatie: String? = null,
        @Argument pageNumber: Int? = 1,
        @Argument verantwoordelijkeOrganisatie: String? = null,
        @Argument zaak: String? = null,
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

    @QueryMapping
    suspend fun getBesluit(
        authentication: CommonGroundAuthentication,
        @Argument besluitId: UUID
    ): Besluit {
        return besluitenService.getBesluit(
            besluitId = besluitId
        )
    }

    @QueryMapping
    suspend fun getBesluitAuditTrails(
        authentication: CommonGroundAuthentication,
        @Argument besluitId: UUID
    ): List<BesluitAuditTrail> {
        return besluitenService.getBesluitAuditTrails(
            besluitId = besluitId
        )
    }

    @QueryMapping
    suspend fun getBesluitAuditTrail(
        authentication: CommonGroundAuthentication,
        @Argument besluitId: UUID,
        @Argument auditTrailId: UUID,
    ): BesluitAuditTrail {
        return besluitenService.getBesluitAuditTrail(
            besluitId = besluitId,
            auditTrailId = auditTrailId
        )
    }

    @QueryMapping
    suspend fun getBesluitDocumenten(
        authentication: CommonGroundAuthentication,
        @Argument besluit: String? = null,
        @Argument informatieobject: String? = null,
    ): List<BesluitDocument> {
        return besluitenService.getBesluitDocumenten(
            besluit = besluit,
            informatieobject = informatieobject
        )
    }

    @QueryMapping
    suspend fun getBesluitDocument(
        authentication: CommonGroundAuthentication,
        @Argument documentId: UUID
    ): BesluitDocument {
        return besluitenService.getBesluitDocument(
            documentId = documentId)
    }

    @SchemaMapping(typeName = "Besluit", field = "auditTrails")
    suspend fun auditTrails(
        besluit: Besluit
    ): List<BesluitAuditTrail> {
        return besluitenService.getBesluitAuditTrails(CoreUtils.extractId(besluit.url))
    }

    @SchemaMapping(typeName = "Besluit", field = "documenten")
    suspend fun documenten(
        besluit: Besluit
    ): List<BesluitDocument> {
        return besluitenService.getBesluitDocumenten(besluit.url)
    }

    @SchemaMapping(typeName = "Besluit", field = "besluittype")
    suspend fun besluittype(
        besluit: Besluit
    ): BesluitType {
        return catalogiApiService.getBesluitType(
            besluitTypeUrl = besluit.besluittype
        )
    }
}
