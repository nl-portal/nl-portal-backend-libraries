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
package nl.nlportal.zakenapi.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import nl.nlportal.besluiten.domain.Besluit
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.catalogiapi.domain.StatusType
import nl.nlportal.catalogiapi.domain.ZaakType
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.zakenapi.service.ZakenApiService
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.*

data class Zaak(
    val uuid: UUID,
    val url: String,
    val identificatie: String,
    val omschrijving: String,
    @GraphQLIgnore
    val zaaktype: String,
    val startdatum: LocalDate,
    val einddatum: LocalDate?,
    @GraphQLIgnore
    val status: String?,
) {
    suspend fun status(
        @GraphQLIgnore
        @Autowired
        zakenApiService: ZakenApiService,
    ): ZaakStatus? {
        return status?.let { zakenApiService.getZaakStatus(it) }
    }

    suspend fun statusGeschiedenis(
        @GraphQLIgnore
        @Autowired
        zakenApiService: ZakenApiService,
    ): List<ZaakStatus> {
        return zakenApiService.getZaakStatusHistory(uuid)
    }

    suspend fun documenten(
        @GraphQLIgnore
        @Autowired
        zakenApiService: ZakenApiService,
    ): List<Document> {
        return zakenApiService.getDocumenten(url)
    }

    suspend fun statussen(
        @GraphQLIgnore
        @Autowired
        catalogiApiService: CatalogiApiService,
    ): List<StatusType> {
        return catalogiApiService.getZaakStatusTypes(zaaktype)
    }

    suspend fun zaaktype(
        @GraphQLIgnore
        @Autowired
        catalogiApiService: CatalogiApiService,
    ): ZaakType {
        return catalogiApiService.getZaakType(zaaktype)
    }

    suspend fun zaakdetails(
        @GraphQLIgnore
        @Autowired
        zakenApiService: ZakenApiService,
    ): ZaakDetails {
        return zakenApiService.getZaakDetails(url)
    }

    suspend fun besluiten(
        @GraphQLIgnore
        @Autowired
        besluitenService: BesluitenService,
    ): List<Besluit> {
        return return besluitenService.getBesluiten(
            zaak = url,
        )
    }
}