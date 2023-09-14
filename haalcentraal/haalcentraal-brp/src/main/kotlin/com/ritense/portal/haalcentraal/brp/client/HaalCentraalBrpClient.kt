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
package com.ritense.portal.haalcentraal.brp.client

import com.ritense.portal.haalcentraal.brp.domain.bewoning.Bewoning
import com.ritense.portal.haalcentraal.brp.domain.persoon.Persoon
import com.ritense.portal.haalcentraal.brp.domain.persoon.PersoonNaam
import com.ritense.portal.haalcentraal.client.HaalCentraalClientProvider
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDate

class HaalCentraalBrpClient(
    val haalCentraalClientProvider: HaalCentraalClientProvider
) {

    suspend fun getPersoon(bsn: String, authentication: Authentication): Persoon {
        return haalCentraalClientProvider.webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/brp/ingeschrevenpersonen/$bsn")
                    .queryParam(
                        "fields",
                        "naam,nationaliteiten,geslachtsaanduiding,geboorte,burgerservicenummer,verblijfplaats"
                    )
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody()
    }

    suspend fun getPersoonNaam(bsn: String, authentication: Authentication): PersoonNaam? {
        return haalCentraalClientProvider.webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/brp/ingeschrevenpersonen/$bsn")
                    .queryParam(
                        "fields",
                        "naam"
                    )
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody<Persoon>()
            .naam
    }

    suspend fun getBewoningen(bsn: String, authentication: Authentication): Bewoning {
        return haalCentraalClientProvider.webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/bewoning/bewoningen")
                    .queryParam("burgerservicenummer", bsn)
                    .queryParam("peildatum", LocalDate.now())
                    .queryParam("fields", "bewoners")
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody()
    }
}