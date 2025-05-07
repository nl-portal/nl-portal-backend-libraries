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
import java.time.ZonedDateTime
import java.util.UUID

data class OpenProductThema(
    val uuid: UUID,
    val naam: String,
    val beschrijving: String? = null,
    val gepubliceerd: Boolean? = false,
    @JsonProperty("hoofd_thema")
    val hoofdThema: UUID? = null,
    val producttypen: List<OpenProductThemaProductType> = emptyList(),
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: ZonedDateTime,
    @JsonProperty("update_datum")
    val updateDatum: ZonedDateTime,
)

data class OpenProductThemaProductType(
    val uuid: UUID,
    val code: String,
    val keywords: List<String> = emptyList(),
    @JsonProperty("uniforme_product_naam")
    val uniformeProductNaam: String,
    @JsonProperty("toegestane_statussen")
    val toegestaneStatussen: List<OpenProductToegestaneStatus> = emptyList(),
    val gepubliceerd: Boolean? = false,
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: ZonedDateTime,
    @JsonProperty("update_datum")
    val updateDatum: ZonedDateTime,
)

enum class OpenProductThemasFilters(
    @JsonValue val value: String,
) : OpenProductFilters {
    PAGE("page"),
    PAGE_SIZE("page_size"),
    NAAM("naam"),
    AANMAAK_DATUM("aanmaak_datum"),
    AANMAAK_DATUM_GTE("aanmaak_datum__gte"),
    AANMAAK_DATUM_LTE("aanmaak_datum__lte"),
    GEPUBLICEERD("gepubliceerd"),
    HOOFDTHEMA_NAAM("hoofd_thema__naam"),
    UPDATE_DATUM("update_datum"),
    UPDATE_DATUM_GTE("update_datum__gte"),
    UPDATE_DATUM_LTE("update_datum__lte"),
    ;

    override fun toString() = this.value
}