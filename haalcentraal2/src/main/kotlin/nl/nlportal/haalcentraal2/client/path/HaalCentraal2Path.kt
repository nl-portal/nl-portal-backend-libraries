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
package nl.nlportal.haalcentraal2.client.path

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import org.springframework.web.util.UriBuilder

open class HaalCentraal2Path {
    open val path: String = "/"

    fun UriBuilder.applyFilters(filters: List<Pair<HaalCentraal2Filters, Any>>? = null): UriBuilder =
        apply {
            filters?.forEach { queryParam(it.first.toString(), it.second.toString()) }
        }
}

@GraphQLIgnore
interface HaalCentraal2Filters