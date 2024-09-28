/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
import nl.nlportal.openklant.domain.CreatePartij
import nl.nlportal.openklant.domain.Partij
import nl.nlportal.openklant.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBody

class Partijen(val client: OpenKlant2Client) : KlantInteractiesPath() {
    override val path = "/klantinteracties/api/v1/partijen"

    suspend fun find(queryParams: MultiValueMap<String, String>? = null): Partij? {
        val response: ResultPage<Partij> =
            client
                .webClient()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                    queryParams?.let { uriBuilder.queryParams(it) }
                    uriBuilder.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response.results.singleOrNull()
    }

    suspend fun create(createPartij: CreatePartij): Partij {
        val response: Partij =
            client
                .webClient()
                .post()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                        .build()
                }
                .body(BodyInserters.fromValue(createPartij))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response
    }

    suspend fun put(partij: Partij): Partij? {
        val response: ResultPage<Partij> =
            client
                .webClient()
                .put()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("$path/${partij.uuid}")
                        .build()
                }
                .body(BodyInserters.fromValue(partij))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response.results.singleOrNull()
    }
}