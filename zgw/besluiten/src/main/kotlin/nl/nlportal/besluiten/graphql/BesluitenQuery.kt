package nl.nlportal.besluiten.graphql

import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.catalogiapi.domain.BesluitType
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.documentenapi.domain.Document
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class BesluitenQuery(
    val besluitenService: BesluitenService,
    val catalogiApiService: CatalogiApiService
) {
    @SchemaMapping(typeName = "Besluit", field = "auditTrails")
    suspend fun auditTrails(
        besluit: Besluit
    ): List<BesluitAuditTrail> {
        return besluitenService.getBesluitAuditTrails(CoreUtils.extractId(besluit.url))
    }

    @SchemaMapping(typeName = "Besluit", field = "documenten")
    suspend fun documenten(
        authentication: CommonGroundAuthentication,
        besluit: Besluit
    ): List<Document> {
        return besluitenService.getBesluitDocumenten(
            authentication = authentication,
            besluit = besluit.url
        )
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
