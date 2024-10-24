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
package nl.nlportal.product.domain

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.core.util.Mapper
import java.util.*

data class ProductType(
    var id: UUID?,
    val naam: String,
    @JsonProperty("subtype")
    val productSubType: String?,
    val omschrijving: String?,
    @GraphQLIgnore
    val statussen: Map<String, String>,
    val zaaktypen: List<UUID>,
    val eigenschappen: ObjectNode?,
    val parameters: ObjectNode?,
    @GraphQLIgnore
    val beslistabelmapping: Map<String, BeslisTabelConfiguration>?,
    @GraphQLIgnore
    val prefillmapping: Map<String, PrefillConfiguration>?,
) {
    @GraphQLDescription("Get list of available beslistabellen, with their object configurations")
    fun beslistabelMappings(): List<String>? {
        return beslistabelmapping?.map { it.key }
    }

    @GraphQLDescription("Get list of available forms to prefill, with their object configurations")
    fun prefillMappings(): ObjectNode? {
        val prefillMap = mutableMapOf<String, Set<String>>()
        prefillmapping?.forEach {
            prefillMap.put(it.key, it.value.variabelen.keys)
        }

        return Mapper.get().convertValue(prefillMap, object : TypeReference<ObjectNode>() {})
    }
}