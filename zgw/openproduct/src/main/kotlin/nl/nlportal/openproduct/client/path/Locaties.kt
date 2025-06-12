/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.client.path

import nl.nlportal.openproduct.client.OpenProductTypeClient
import nl.nlportal.openproduct.client.domain.OpenProductLocatie
import nl.nlportal.openproduct.client.domain.OpenProductLocatiesFilters
import nl.nlportal.openproduct.client.domain.ResultPage
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody
import java.util.*

class Locaties(
    val client: OpenProductTypeClient,
) : OpenProductPath() {
    override val path: String = "/locaties"

    suspend fun get(searchFilters: List<Pair<OpenProductLocatiesFilters, Any>>? = null): ResultPage<OpenProductLocatie> =
        client
            .webClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path(path)
                    .applyFilters(searchFilters)
                uriBuilder.build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()

    suspend fun get(id: UUID): OpenProductLocatie? =
        client
            .webClient
            .get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("$path/$id")
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()
}