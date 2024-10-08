/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.client.path

import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdres
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdressenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificator
import nl.nlportal.openklant.client.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.util.UUID

class DigitaleAdressen(val client: OpenKlant2KlantinteractiesClient) : KlantInteractiesPath() {
    override val path: String = "/digitale-adressen"

    suspend fun get(searchFilters: List<Pair<OpenKlant2DigitaleAdressenFilters, Any>>? = null): List<OpenKlant2PartijIdentificator> {
        val response: ResultPage<OpenKlant2PartijIdentificator> =
            client
                .webClient()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                        .applyFilters(searchFilters)
                    uriBuilder.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response.results
    }

    suspend fun create(digitaleAdres: OpenKlant2DigitaleAdres): OpenKlant2DigitaleAdres? {
        val response: OpenKlant2DigitaleAdres? =
            client
                .webClient()
                .post()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                        .build()
                }
                .body(BodyInserters.fromValue(digitaleAdres))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBodyOrNull()

        return response
    }

    suspend fun put(digitaleAdres: OpenKlant2DigitaleAdres): OpenKlant2DigitaleAdres? {
        val response: OpenKlant2DigitaleAdres? =
            client
                .webClient()
                .put()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("$path/${digitaleAdres.uuid}")
                        .build()
                }
                .body(BodyInserters.fromValue(digitaleAdres))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBodyOrNull()

        return response
    }

    suspend fun delete(uuid: UUID) {
        client
            .webClient()
            .delete()
            .uri { uriBuilder ->
                uriBuilder
                    .path("$path/$uuid")
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBodilessEntity()
    }
}