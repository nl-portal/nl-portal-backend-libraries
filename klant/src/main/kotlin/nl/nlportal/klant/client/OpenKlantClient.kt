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
package nl.nlportal.klant.client

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.klant.generiek.domain.ResultPage
import nl.nlportal.klant.domain.klanten.Klant
import nl.nlportal.klant.domain.klanten.KlantCreationRequest
import nl.nlportal.klant.generiek.client.OpenKlantClientProvider
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody

class OpenKlantClient(
    private val openKlantClientProvider: OpenKlantClientProvider,
) {
    suspend fun getKlanten(authentication: CommonGroundAuthentication, page: Int, bsn: String?): List<Klant> {
        return openKlantClientProvider.webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/klanten/api/v1/klanten")
                    .queryParam("page", page)
                bsn?.let { uriBuilder.queryParam("subjectNatuurlijkPersoon__inpBsn", it) }
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody<ResultPage<Klant>>()
            .results
    }

    suspend fun patchKlant(authentication: CommonGroundAuthentication, klantUrl: String, klant: Klant): Klant {
        return openKlantClientProvider.webClient(authentication)
            .patch()
            .uri(klantUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(klant)
            .retrieve()
            .awaitBody()
    }

    suspend fun postKlant(authentication: CommonGroundAuthentication, klant: KlantCreationRequest): Klant {
        return openKlantClientProvider.webClient(authentication)
            .post()
            .uri("/klanten/api/v1/klanten")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(klant)
            .retrieve()
            .awaitBody()
    }
}