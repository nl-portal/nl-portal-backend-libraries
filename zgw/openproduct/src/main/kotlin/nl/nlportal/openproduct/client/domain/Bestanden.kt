/*
 * Copyright 2025 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class OpenProductBestand(
    val uuid: UUID,
    val bestand: String,
    @JsonProperty("producttype_uuid") val productTypeUuid: UUID,
)

enum class OpenProductBestandenFilters(
    @JsonValue val value: String,
) : OpenProductFilters {
    PAGE("page"),
    PAGE_SIZE("page_size"),
    NAAM_CONTAINS("naam__contains"),
    PRODUCTTYPE_CODE("producttype__code"),
    PRODUCTTYPE_NAAM("producttype__naam"),
    PRODUCTTYPE_UUID("producttype__uuid"),
    UNIFORM_PRODUCT_NAAM("uniforme_product_naam"),
    ;

    override fun toString() = this.value
}