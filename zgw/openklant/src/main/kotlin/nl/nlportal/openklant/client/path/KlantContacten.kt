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
import nl.nlportal.openklant.client.domain.OpenKlant2Klantcontact
import nl.nlportal.openklant.client.domain.OpenKlant2KlantcontactenFilters
import nl.nlportal.openklant.client.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import java.util.UUID

class KlantContacten(
    val client: OpenKlant2KlantinteractiesClient,
) : KlantInteractiesPath() {
    override val path: String = "/klantcontacten"

    suspend fun get(searchFilters: List<Pair<OpenKlant2KlantcontactenFilters, Any>>? = null): List<OpenKlant2Klantcontact> =
        client
            .webClient()
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(path)
                    .applyFilters(searchFilters)
                uriBuilder.build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<ResultPage<OpenKlant2Klantcontact>>()
            .results

    suspend fun get(klantContactId: UUID): OpenKlant2Klantcontact? =
        client
            .webClient()
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("$path/$klantContactId")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBodyOrNull()
}