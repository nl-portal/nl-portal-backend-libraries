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
package nl.nlportal.klant.contactmomenten.client

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.klant.contactmomenten.domain.ContactMoment
import nl.nlportal.klant.generiek.client.OpenKlantClientProvider
import nl.nlportal.klant.generiek.domain.ResultPage
import org.springframework.web.reactive.function.client.awaitBody

class KlantContactMomentenClient(
    private val openKlantClientProvider: OpenKlantClientProvider
) {
    suspend fun getKlantContactMomenten(authentication: CommonGroundAuthentication, klant: String, page: Int): ResultPage<ContactMoment> {
        return openKlantClientProvider.webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/contactmomenten/api/v1/contactmomenten")
                    .queryParam("page", page)
                    .queryParam("klant", klant)
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody<ResultPage<ContactMoment>>()
    }

    suspend fun getObjectContactMomenten(authentication: CommonGroundAuthentication, objectUrl: String, page: Int): ResultPage<ContactMoment> {
        return openKlantClientProvider.webClient(authentication)
            .get()
            .uri {
                val uriBuilder = it.path("/contactmomenten/api/v1/contactmomenten")
                    .queryParam("page", page)
                    .queryParam("object", objectUrl)
                uriBuilder.build()
            }
            .retrieve()
            .awaitBody<ResultPage<ContactMoment>>()
    }
}