/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.zaak.domain.zaken

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.ritense.portal.zaak.domain.catalogi.StatusType
import com.ritense.portal.zaak.domain.documenten.Document
import com.ritense.portal.zaak.service.ZaakService
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

data class Zaak(
    val uuid: UUID,
    val url: String,
    val identificatie: String,
    val omschrijving: String,
    @GraphQLIgnore
    val zaaktype: String,
    val startdatum: LocalDate,
    @GraphQLIgnore
    val status: String?
) {
    suspend fun status(@GraphQLIgnore @Autowired zaakService: ZaakService): ZaakStatus? {
        return status?.let { zaakService.getZaakStatus(it) }
    }

    suspend fun statussen(@GraphQLIgnore @Autowired zaakService: ZaakService): List<StatusType> {
        return zaakService.getZaakStatusTypes(zaaktype)
    }

    suspend fun statusGeschiedenis(@GraphQLIgnore @Autowired zaakService: ZaakService): List<ZaakStatus> {
        return zaakService.getZaakStatusHistory(uuid)
    }

    suspend fun zaaktype(@GraphQLIgnore @Autowired zaakService: ZaakService): ZaakType {
        return zaakService.getZaakType(zaaktype)
    }

    suspend fun documenten(@GraphQLIgnore @Autowired zaakService: ZaakService): List<Document> {
        return zaakService.getDocumenten(url)
    }
}