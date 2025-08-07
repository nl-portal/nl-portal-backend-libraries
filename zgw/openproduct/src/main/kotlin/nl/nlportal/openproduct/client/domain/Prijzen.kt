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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDate
import java.util.UUID

data class OpenProductPrijs(
    val uuid: UUID,
    val prijsopties: List<OpenProductPrijsOptie> = emptyList(),
    val prijsregels: List<OpenProductPrijsRegel> = emptyList(),
    @JsonProperty("actief_vanaf")
    val actiefVanaf: LocalDate,
)

data class OpenProductPrijsOptie(
    val uuid: UUID,
    val bedrag: Float,
    val beschrijving: String,
)

data class OpenProductPrijsRegel(
    val uuid: UUID,
    val url: String,
    val beschrijving: String,
)

enum class OpenProductPrijzenFilters(
    @JsonValue val value: String,
) : OpenProductFilters {
    PAGE("page"),
    PAGE_SIZE("page_size"),
    ACTIEF_VANAF("actief_vanaf"),
    ACTIEF_VANAF_GTE("actief_vanaf__gte"),
    ACTIEF_VANAF_LTE("actief_vanaf__lte"),
    PRIJSOPTIES_BEDRAG("prijsopties__bedrag"),
    PRIJSOPTIES_BEDRAGF_GTE("actief_vanaf__gte"),
    PRIJSOPTIES_BEDRAG_LTE("actief_vanaf__lte"),
    PRIJSOPTIES_BESCHRIJVING("prijsopties__beschrijving"),
    PRIJSREGELS_BESCHRIJVING("prijsregels__beschrijving"),
    PRIJSREGELS_DMN_TABEL_ID("prijsregels__dmn_tabel_id"),
    PRODUCTTYPE_CODE("producttype__code"),
    PRODUCTTYPE_NAAM("producttype__naam"),
    PRODUCTTYPE_UUID("producttype__uuid"),
    UNIFORM_PRODUCT_NAAM("uniforme_product_naam"),
    ;

    override fun toString() = this.value
}