package nl.nlportal.besluiten.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.catalogiapi.domain.BesluitType
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.core.util.CoreUtils
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

data class Besluit(
    val url: String,
    val identificatie: String,
    val verantwoordelijkeOrganisatie: String,
    @GraphQLIgnore
    val besluittype: String,
    val zaak: String,
    val datum: LocalDate,
    val toelichting: String?,
    val bestuursorgaan: String?,
    val ingangsdatum: LocalDate,
    val vervaldatum: LocalDate?,
    val vervalreden: String,
    val vervalredenWeergave: String,
    val publicatiedatum: LocalDate?,
    val verzenddatum: LocalDate?,
    val uiterlijkeReactiedatum: LocalDate?,
) {
    suspend fun auditTrails(
        @GraphQLIgnore
        @Autowired
        besluitenService: BesluitenService,
    ): List<BesluitAuditTrail> {
        return besluitenService.getBesluitAuditTrails(CoreUtils.extractId(url))
    }

    suspend fun documenten(
        @GraphQLIgnore
        @Autowired
        besluitenService: BesluitenService,
    ): List<BesluitDocument> {
        return besluitenService.getBesluitDocumenten(url)
    }

    suspend fun besluittype(
        @GraphQLIgnore @Autowired
        catalogiApiService: CatalogiApiService,
    ): BesluitType {
        return catalogiApiService.getBesluitType(
            besluitTypeUrl = besluittype
        )
    }
}
