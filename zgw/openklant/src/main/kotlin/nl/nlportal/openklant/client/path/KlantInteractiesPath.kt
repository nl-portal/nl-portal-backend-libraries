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

import nl.nlportal.openklant.client.domain.OpenKlant2Filters
import org.springframework.web.util.UriBuilder

open class KlantInteractiesPath {
    open val path: String = "/"

    fun UriBuilder.queryParams(filters: List<Pair<OpenKlant2Filters, String>>? = null): UriBuilder {
        return apply {
            filters?.forEach { queryParam(it.first.toString(), it.second) }
        }
    }
}