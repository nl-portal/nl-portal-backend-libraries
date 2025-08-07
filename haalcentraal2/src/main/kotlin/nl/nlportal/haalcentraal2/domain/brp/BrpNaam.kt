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
package nl.nlportal.haalcentraal2.domain.brp

import com.fasterxml.jackson.annotation.JsonValue

data class BrpNaam(
    val voornamen: String? = null,
    val adellijkeTitelPredicaat: BrpAdellijkeTitelPredicaat? = null,
    val voorletters: String? = null,
    val voorvoegsel: String? = null,
    val geslachtsnaam: String? = null,
    val aanduidingNaamgebruik: BrpCodeOmschrijving? = null,
    val volledigeNaam: String? = null,
    val inOnderzoek: BrpNaamInOnderzoek? = null,
    var officialLastName: String? = null,
) {
    fun lastName(): String =
        if (voorvoegsel != null && geslachtsnaam != null) {
            "$voorvoegsel $geslachtsnaam"
        } else {
            geslachtsnaam ?: ""
        }
}

data class BrpAdellijkeTitelPredicaat(
    val soort: String? = null,
    val omschrijving: String? = null,
    val code: String? = null,
)

data class BrpNaamInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val voornamen: Boolean? = false,
    val adellijkeTitelPredicaat: Boolean? = false,
    val voorletters: Boolean? = false,
    val voorvoegsel: Boolean? = false,
    val geslachtsnaam: Boolean? = false,
    val aanduidingNaamgebruik: Boolean? = false,
    val volledigeNaam: Boolean? = false,
)

enum class AanduidingNaamGebruikBrpNaam(
    @JsonValue val value: String,
) {
    EIGEN("E"),
    EIGEN_PARTNER("N"),
    PARTNER("P"),
    PARTNER_EIGEN("V"),
}