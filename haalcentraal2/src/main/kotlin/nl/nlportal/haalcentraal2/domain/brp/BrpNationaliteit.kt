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

data class BrpNationaliteit(
    val type: String,
    val redenOpname: BrpCodeOmschrijving,
    val datumIngangGeldigheid: BrpCodeOmschrijving? = null,
    val nationaliteit: BrpCodeOmschrijving? = null,
    val inOnderzoek: Brp2NationaliteitInOnderzoek? = null,
)

data class Brp2NationaliteitInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val type: Boolean? = false,
    val redenOpname: Boolean? = false,
    val datumIngangGeldigheid: Boolean? = false,
    val nationaliteit: Boolean? = false,
)