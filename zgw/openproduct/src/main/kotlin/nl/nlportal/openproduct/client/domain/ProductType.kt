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
package nl.nlportal.openproduct.client.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.time.ZonedDateTime
import java.util.UUID

data class OpenProductProductType(
    val uuid: UUID,
    val code: String,
    val naam: String,
    val samenvatting: String,
    val taal: String,
    val keywords: List<String> = emptyList(),
    @JsonProperty("uniforme_product_naam")
    val uniformeProductNaam: String,
    @JsonProperty("toegestane_statussen")
    val toegestaneStatussen: List<OpenProductToegestaneStatus> = emptyList(),
    @JsonProperty("interne_opmerkingen")
    val interneOpmerking: String? = null,
    val gepubliceerd: Boolean? = false,
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: ZonedDateTime,
    @JsonProperty("update_datum")
    val updateDatum: ZonedDateTime,
    val themas: List<OpenProductProductTypeThema> = emptyList(),
    val locaties: List<OpenProductLocatie> = emptyList(),
    val organisaties: List<OpenProductOrganisatie> = emptyList(),
    val contacten: List<OpenProductContact> = emptyList(),
    val prijzen: List<OpenProductPrijs> = emptyList(),
    val links: List<OpenProductLink> = emptyList(),
    val acties: List<OpenProductActie> = emptyList(),
    val bestanden: List<OpenProductProductTypeBestand> = emptyList(),
    @JsonProperty("externe_codes")
    val externCodes: List<OpenProductProductTypeExterneCode> = emptyList(),
    val parameters: List<OpenProductProductTypeParameter> = emptyList(),
    @JsonProperty("verbruiksobject_schema")
    val verbruiksObjectSchema: OpenProductSchema? = null,
    @JsonProperty("dataobject_schema")
    val dataObjectSchema: OpenProductSchema? = null,
    val zaaktypen: List<OpenProductUrl> = emptyList(),
    val verzoektypen: List<OpenProductUrl> = emptyList(),
    val processen: List<OpenProductUrl> = emptyList(),
)

data class OpenProductProductTypeContent(
    val uuid: UUID,
    val taal: String,
    val content: String,
    val labels: List<String>? = emptyList(),
)

data class OpenProductProductTypeThema(
    val uuid: UUID,
    val naam: String,
    val beschrijving: String? = null,
    val gepubliceerd: Boolean? = false,
    @JsonProperty("hoofd_thema")
    val hoofdThema: String? = null,
    val producttypen: List<OpenProductThemaProductType> = emptyList(),
    @JsonProperty("aanmaak_datum")
    val aanmaakDatum: ZonedDateTime,
    @JsonProperty("update_datum")
    val updateDatum: ZonedDateTime,
)

data class OpenProductProductTypeBestand(
    val uuid: UUID,
    val bestand: String,
)

data class OpenProductProductTypeExterneCode(
    val naam: String,
    val code: String,
)

data class OpenProductProductTypeParameter(
    val naam: String,
    val waarde: String,
)

enum class OpenProductProductTypesFilters(
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
    CODE("code"),
    NAAM("naam"),
    EXTERNE_CODE("externe_code"),
    KEYWORDS("keywords"),
    LETTERS("letter"),
    PARAMETER("parameter"),
    PROCESS_UUID("processen__uuid"),
    THEMAS_NAAM("themas__naam"),
    THEMAS_NAAM_IN("themas__naam__in"),
    THEMAS_UUID("themas__uuid"),
    THEMAS_UUID_IN("themas__uuid__in"),
    TOEGESTANE_STATUSSEN("toegestane_statussen"),
    UNIFORM_PRODUCT_NAAM("uniforme_product_naam"),
    VERZOEKTYPE_UUID("verzoektypen__uuid"),
    ZAAKTYPE_UUID("zaaktypen__uuid"),
    VERBRUIKSOBJECT_SCHEMA_NAAM("verbruiksobject_schema__naam"),
    ;

    override fun toString() = this.value
}