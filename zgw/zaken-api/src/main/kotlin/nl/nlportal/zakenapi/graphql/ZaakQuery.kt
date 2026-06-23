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
package nl.nlportal.zakenapi.graphql

import java.util.UUID
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.domain.BesluitAuditTrail
import nl.nlportal.besluiten.domain.BesluitDocument
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.catalogiapi.domain.BesluitType
import nl.nlportal.catalogiapi.domain.ResultaatType
import nl.nlportal.catalogiapi.domain.StatusType
import nl.nlportal.catalogiapi.domain.ZaakStatusType
import nl.nlportal.catalogiapi.domain.ZaakType
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zakenapi.domain.ZaakDetails
import nl.nlportal.zakenapi.domain.ZaakResultaat
import nl.nlportal.zakenapi.domain.ZaakStatus
import nl.nlportal.zakenapi.domain.ZaakSubStatus
import nl.nlportal.zakenapi.service.ZakenApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
open class ZaakQuery(
    val zakenApiService: ZakenApiService,
    val besluitenService: BesluitenService,
    val catalogiApiService: CatalogiApiService,
) {
    @QueryMapping
    open suspend fun getZaken(
        authentication: CommonGroundAuthentication,
        @Argument page: Int? = 1,
        @Argument pageSize: Int? = null,
        @Argument zaakTypeUrl: String? = null,
        @Argument isOpen: Boolean? = null,
        @Argument identificatie: String? = null,
        @Argument omschrijving: String? = null,
        @Argument identificatieContains: String? = null,
    ): ZaakPage {
        return zakenApiService.getZaken(
            page = page ?: 1,
            pageSize = pageSize,
            authentication = authentication,
            zaakTypeUrl = zaakTypeUrl,
            isOpen = isOpen,
            identificatie = identificatie,
            omschrijving = omschrijving,
            identificatieContains = identificatieContains,
        )
    }

    @QueryMapping
    open suspend fun getZaak(
        @Argument id: UUID,
        authentication: CommonGroundAuthentication,
    ): Zaak {
        return zakenApiService.getZaak(
            id = id,
            authentication = authentication
        )
    }

    @SchemaMapping(typeName = "Zaak", field = "status")
    suspend fun status(
        zaak: Zaak,
    ): ZaakStatus? {
        return zaak.status?.let { zakenApiService.getZaakStatus(it) }
    }

    @SchemaMapping(typeName = "Zaak", field = "statusGeschiedenis")
    suspend fun statusGeschiedenis(
        zaak: Zaak,
    ): List<ZaakStatus> {
        return zakenApiService.getZaakStatusHistory(zaak.uuid)
    }

    @SchemaMapping(typeName = "Zaak", field = "documenten")
    suspend fun documenten(
        zaak: Zaak,
    ): List<Document> {
        return zakenApiService.getDocumenten(zaak.url)
    }

    @SchemaMapping(typeName = "Zaak", field = "statussen")
    suspend fun statussen(
        zaak: Zaak,
    ): List<StatusType> {
        return catalogiApiService.getZaakStatusTypes(zaak.zaaktype)
    }

    @SchemaMapping(typeName = "Zaak", field = "zaaktype")
    suspend fun zaaktype(
        zaak: Zaak,
    ): ZaakType {
        return catalogiApiService.getZaakType(zaak.zaaktype)
    }

    @SchemaMapping(typeName = "Zaak", field = "zaakdetails")
    suspend fun zaakdetails(
        zaak: Zaak,
    ): ZaakDetails {
        return zakenApiService.getZaakDetails(zaak.url)
    }

    @SchemaMapping(typeName = "Zaak", field = "besluiten")
    suspend fun besluiten(
        zaak: Zaak,
    ): List<Besluit> {
        return besluitenService.getBesluiten(
            zaak = zaak.url,
        )
    }

    @SchemaMapping(typeName = "Zaak", field = "resultaat")
    suspend fun resultaat(
        zaak: Zaak,
    ): ZaakResultaat? {
        return zaak.resultaat?.let {
            zakenApiService.getZaakResultaat(it)
        }
    }

    @SchemaMapping(typeName = "ZaakResultaat", field = "resultaattype")
    suspend fun resultaattype(
        zaakResultaat: ZaakResultaat,
    ): ResultaatType {
        return catalogiApiService.getResultaatType(
            resultaatTypeUrl = zaakResultaat.resultaattype
        )
    }

    @SchemaMapping(typeName = "ZaakStatus", field = "statustype")
    suspend fun statustype(
        zaakStatus: ZaakStatus
    ): ZaakStatusType {
        return catalogiApiService.getZaakStatusType(zaakStatus.statustype)
    }

    @SchemaMapping(typeName = "ZaakStatus", field = "substatussen")
    suspend fun substatussen(
        zaakStatus: ZaakStatus
    ): List<ZaakSubStatus> {
        return zakenApiService.getZaakSubStatussen(
            zaakUrl = zaakStatus.zaak,
            statusUrl = zaakStatus.url
        )
    }
}
