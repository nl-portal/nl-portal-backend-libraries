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

import nl.nlportal.openklant.client.OpenKlant2Client
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificator
import nl.nlportal.openklant.client.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMapAdapter
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBody

class PartijIdentificatoren(val client: OpenKlant2Client) : KlantInteractiesPath() {
    override val path: String = "/partij-identificatoren"

    suspend fun find(searchVariables: MultiValueMapAdapter<String, String>? = null): List<OpenKlant2PartijIdentificator> {
        val response: ResultPage<OpenKlant2PartijIdentificator> =
            client
                .webClient()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                    searchVariables?.let { uriBuilder.queryParams(it) }
                    uriBuilder.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response.results
    }

    suspend fun create(partijIdentificatorRequest: OpenKlant2PartijIdentificator): OpenKlant2PartijIdentificator {
        val response: OpenKlant2PartijIdentificator =
            client
                .webClient()
                .post()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                        .build()
                }
                .body(BodyInserters.fromValue(partijIdentificatorRequest))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response
    }
}