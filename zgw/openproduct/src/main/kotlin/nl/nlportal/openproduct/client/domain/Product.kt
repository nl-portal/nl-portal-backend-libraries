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
import tools.jackson.databind.node.ObjectNode
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class OpenProductProduct(
    val uuid: UUID,
    val url: String? = null,
    val naam: String,
    @JsonProperty("start_datum")
    val startDatum: LocalDate? = null,
    @JsonProperty("eind_datum")
    val eindDatum: LocalDate? = null,
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: ZonedDateTime,
    @JsonProperty("update_datum")
    val updateDatum: ZonedDateTime,
    val producttype: OpenProductProductProductType,
    val gepubliceerd: Boolean? = false,
    val eigenaren: List<OpenProductProductEigenaar> = emptyList(),
    val documenten: List<OpenProductUrl> = emptyList(),
    val status: OpenProductToegestaneStatus,
    val prijs: Float? = null,
    val frequentie: OpenProductFrequentie,
    val verbruiksobject: ObjectNode? = null,
    val dataobject: ObjectNode? = null,
    val zaken: List<OpenProductUrl>? = emptyList(),
    val taken: List<OpenProductUrl>? = emptyList(),
    @JsonProperty("aanvraag_zaak_urn")
    val aanvraagZaakUrn: String? = null,
    @JsonProperty("aanvraag_zaak_url")
    val aanvraagZaakUrl: String? = null,
)

data class OpenProductProductUpdate(
    val uuid: UUID,
    val verbruiksobject: Any? = null,
    val dataobject: Any? = null,
)

data class OpenProductProductProductType(
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
    @JsonProperty("publicatie_start_datum")
    val publicatieStartDatum: LocalDate? = null,
    @JsonProperty("publicatie_eind_datum")
    val publicatieEindDatum: LocalDate? = null,
    val themas: List<OpenProductEmbeddedThema> = emptyList(),
)

data class OpenProductEmbeddedThema(
    val uuid: UUID,
    val naam: String,
    val beschrijving: String? = null,
    val gepubliceerd: Boolean? = false,
    @JsonProperty("hoofd_thema")
    val hoofdThema: String? = null,
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: ZonedDateTime,
    @JsonProperty("update_datum")
    val updateDatum: ZonedDateTime,
)

data class OpenProductProductEigenaar(
    val uuid: UUID,
    val bsn: String? = null,
    @JsonProperty("kvk_nummer")
    val kvkNummer: String? = null,
    val vestigingsnummer: String? = null,
    val klantnummer: String? = null,
)

enum class OpenProductProductenFilters(
    @JsonValue val value: String,
) : OpenProductFilters {
    PAGE("page"),
    PAGE_SIZE("page_size"),
    AANMAAK_DATUM("aanmaak_datum"),
    AANMAAK_DATUM_GTE("aanmaak_datum__gte"),
    AANMAAK_DATUM_LTE("aanmaak_datum__lte"),
    GEPUBLICEERD("gepubliceerd"),
    UPDATE_DATUM("update_datum"),
    UPDATE_DATUM_GTE("update_datum__gte"),
    UPDATE_DATUM_LTE("update_datum__lte"),
    DATAOBJECT_ATTR("dataobject_attr"),
    VERBRUIKSOBJECT_ATTR("verbruiksobject_attr"),
    DOCUMENTEN_UUID("documenten__uuid"),
    EIGENAREN_BSN("eigenaren__bsn"),
    EIGENAREN_KLANTNUMMER("eigenaren__klantnummer"),
    EIGENAREN_UUID("eigenaren__uuid"),
    EIGENAREN_KVKNUMMER("eigenaren__kvk_nummer"),
    EIGENAREN_VESTIGINGSNUMMER("eigenaren__vestigingsnummer"),
    EIND_DATUM("eind_datum_datum"),
    EIND_DATUM_GTE("eind_datum__gte"),
    EIND_DATUM_LTE("eind_datum__lte"),
    FREQUENTIE("frequentie"),
    PRIJS_DATUM("prijs"),
    PRIJS_DATUM_GTE("prijs__gte"),
    PRIJS_DATUM_LTE("prijs__lte"),
    PRODUCTTYPE_NAAM("producttype__naam"),
    PRODUCTTYPE_NAAM_IN("producttype__naam__in"),
    PRODUCTTYPE_UUID("producttype__uuid"),
    PRODUCTTYPE_UUID_IN("producttype__uuid__in"),
    PRODUCTTYPE_CODE("producttype__code"),
    PRODUCTTYPE_CODE_IN("producttype__code__in"),
    START_DATUM("start_datum_datum"),
    START_DATUM_GTE("start_datum__gte"),
    START_DATUM_LTE("start_datum__lte"),
    STATUS("status"),
    UNIFORM_PRODUCT_NAAM("uniforme_product_naam"),
    THEMA_NAAM("producttype__themas__naam"),
    THEMA_NAAM_IN("producttype__themas__naam__in"),
    THEMA_UUID("producttype__themas__uuid"),
    THEMA_UUID_IN("producttype__themas__uuid__in"),
    ;

    override fun toString() = this.value
}