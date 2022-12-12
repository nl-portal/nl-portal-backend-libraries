/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.erfpachtdossier.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Erfpachtdossier(
    @JsonProperty("dossier_nummer")
    val dossierNummer: String,
    val kadaster: List<String>,
    @JsonProperty("eerste_uitgifte")
    val eersteUitgifte: LocalDate,
    @JsonProperty("beeindiging_uitgifte")
    val beeindigingUitgifte: LocalDate,
    @JsonProperty("einde_reden")
    val eindeReden: String,
    @JsonProperty("tijdvak_begin")
    val tijdvakBegin: String,
    @JsonProperty("tijdvak_einde")
    val tijdvakEinde: String,
    @JsonProperty("ab_juridisch")
    val abJuridisch: String,
    @JsonProperty("te_betalen_canon")
    val teBetalenCanon: Double,
    val adres_straatnaam: String,
    val adres_nummer: Int,
    val adres_letter: String,
    val adres_toevoeging: String,
    val adres_postcode: String,
    @JsonProperty("Adres_gemeente")
    val adres_gemeente: String,
    val bestemming: List<ErfpachtdossierBestemming>,
    @JsonProperty("mut_begindatum")
    val mutBeginDatum: LocalDate,
    @JsonProperty("mut_einddatum")
    val mutEindDatum: LocalDate
)