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
package nl.nlportal.openproduct.client.domain

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.core.util.Mapper
import java.util.UUID

data class OpenProductActie(
    val uuid: UUID,
    val naam: String,
    val url: String,
    @JsonProperty("producttype_uuid")
    val productTypeUuid: UUID? = null,
    @GraphQLIgnore
    val mapping: Map<String, List<OpenProductActieMappingVariable>> = emptyMap(),
) {
    fun mapping(): List<ObjectNode> = Mapper.get().convertValue(mapping, object : TypeReference<List<ObjectNode>>() {})
}

data class OpenProductActieMappingVariable(
    val name: String,
    val classType: OpenProductDmnVariableType,
    val regex: String?,
    val value: String?,
)

data class OpenProductDmnResponse(
    val value: String,
    val type: String,
)

data class OpenProductDmnRequest(
    val key: String,
    val mapping: OpenProductDmnRequestMapping,
)

data class OpenProductDmnRequestMapping(
    val variables: Map<String, OpenProductDmnVariable>,
)

data class OpenProductDmnVariable(
    val value: Any,
    val type: OpenProductDmnVariableType,
)

enum class OpenProductDmnVariableType(
    @JsonValue val value: String,
) {
    STRING("String"),
    INTEGER("Integer"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    DATE("Date"),
    LONG("Long"),
}

enum class OpenProductActiesFilters(
    @JsonValue val value: String,
) : OpenProductFilters {
    PAGE("page"),
    PAGE_SIZE("page_size"),
    NAAM("naam"),
    NAAM_CONTAINS("naam__contains"),
    DMN_CONFIG_NAAM("dmn_config__naam"),
    DMN_CONFIG_TABEL_ENDPOINT("dmn_config__tabel_endpoint"),
    DMN_TABEL_ID("dmn_tabel_id"),
    PRODUCTTYPE_CODE("producttype__code"),
    PRODUCTTYPE_NAAM("producttype__naam"),
    PRODUCTTYPE_UUID("producttype__uuid"),
    UNIFORM_PRODUCT_NAAM("uniforme_product_naam"),
    ;

    override fun toString() = this.value
}