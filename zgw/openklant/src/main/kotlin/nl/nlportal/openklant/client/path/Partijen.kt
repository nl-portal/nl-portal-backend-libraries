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
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters
import nl.nlportal.openklant.client.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.util.UUID

class Partijen(val client: OpenKlant2Client) : KlantInteractiesPath() {
    override val path = "/partijen"

    suspend fun find(searchFilters: List<Pair<OpenKlant2PartijenFilters, String>>? = null): OpenKlant2Partij? {
        val response: ResultPage<OpenKlant2Partij> =
            client
                .webClient()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                        .queryParams(filters = searchFilters)
                    uriBuilder.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response.results.singleOrNull()
    }

    suspend fun get(partijId: UUID): OpenKlant2Partij? {
        val response: OpenKlant2Partij? =
            client
                .webClient()
                .get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("$path/$partijId")
                        .build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBodyOrNull()

        return response
    }

    suspend fun create(partijRequest: OpenKlant2Partij): OpenKlant2Partij {
        val response: OpenKlant2Partij =
            client
                .webClient()
                .post()
                .uri { uriBuilder ->
                    uriBuilder
                        .path(path)
                        .build()
                }
                .body(BodyInserters.fromValue(partijRequest))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody()

        return response
    }

    suspend fun put(partij: OpenKlant2Partij): OpenKlant2Partij? {
        val response: ResultPage<OpenKlant2Partij> =
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