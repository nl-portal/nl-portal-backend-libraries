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
package nl.nlportal.catalogiapi.service

import nl.nlportal.catalogiapi.client.CatalogiApiClient
import nl.nlportal.catalogiapi.domain.BesluitType
import nl.nlportal.catalogiapi.domain.StatusType
import nl.nlportal.catalogiapi.domain.ZaakStatusType
import nl.nlportal.catalogiapi.domain.ZaakType
import nl.nlportal.core.util.CoreUtils.extractId

class CatalogiApiService(
    val catalogiApiClient: CatalogiApiClient,
) {
    suspend fun getZaakStatusTypes(zaakType: String): List<StatusType> {
        return catalogiApiClient.getStatusTypes(zaakType).sortedBy { it.volgnummer }
    }

    suspend fun getZaakStatusType(statusTypeUrl: String): ZaakStatusType {
        return catalogiApiClient.getStatusType(extractId(statusTypeUrl))
    }

    suspend fun getZaakType(zaakTypeUrl: String): ZaakType {
        return catalogiApiClient.getZaakType(extractId(zaakTypeUrl))
    }

    suspend fun getBesluitTypes(zaakType: String): List<BesluitType> {
        return catalogiApiClient.getBesluitTypes(zaakType)
    }

    suspend fun getBesluitType(besluitTypeUrl: String): BesluitType {
        return catalogiApiClient.getBesluitType(extractId(besluitTypeUrl))
    }
}