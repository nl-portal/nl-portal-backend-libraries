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

data class BrpPartner(
    val burgerservicenummer: String? = null,
    val geslacht: BrpCodeOmschrijving? = null,
    val soortVerbintenis: BrpCodeOmschrijving? = null,
    val inOnderzoek: BrpPartnerInOnderzoek? = null,
    val naam: BrpNaam? = null,
    val geboorte: BrpDatumLandPlaats? = null,
    val aangaanHuwelijkPartnerschap: BrpPartnerHuwelijkAangaan? = null,
    val ontbindingHuwelijkPartnerschap: BrpPartnerHuwelijkOntbinding? = null,
)

data class BrpPartnerInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val burgerservicenummer: Boolean? = false,
    val geslacht: Boolean? = false,
    val soortVerbintenis: Boolean? = false,
)

data class BrpPartnerHuwelijkAangaan(
    val datum: BrpDatum? = null,
    val land: BrpCodeOmschrijving? = null,
    val plaats: BrpCodeOmschrijving? = null,
    val soortVerbintenis: BrpCodeOmschrijving? = null,
    val inOnderzoek: BrpPartnerHuwelijkAangaanInOnderzoek? = null,
)

data class BrpPartnerHuwelijkAangaanInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val land: Boolean? = false,
    val plaats: Boolean? = false,
    val soortVerbintenis: Boolean? = false,
    val datum: Boolean? = false,
)

data class BrpPartnerHuwelijkOntbinding(
    val datum: BrpDatum? = null,
    val inOnderzoek: BrpPartnerHuwelijkOntbindingInOnderzoek? = null,
)

data class BrpPartnerHuwelijkOntbindingInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val datum: Boolean? = false,
)