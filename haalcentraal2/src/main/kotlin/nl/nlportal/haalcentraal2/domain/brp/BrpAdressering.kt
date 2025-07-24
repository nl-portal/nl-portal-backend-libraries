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

data class BrpAdressering(
    val adresregel1: String? = null,
    val adresregel2: String? = null,
    val adresregel3: String? = null,
    val land: BrpCodeOmschrijving? = null,
    val indicatieVastgesteldVerblijftNietOpAdres: Boolean? = false,
    val aanhef: String? = null,
    val aanschrijfwijze: BrpAdresseringAanschrijfwijze? = null,
    val gebruikInLopendeTekst: String? = null,
)

data class BrpAdresseringAanschrijfwijze(
    val naam: String? = null,
    val aanspreekvorm: String? = null,
)

data class BrpAdresseringInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val adresregel1: Boolean? = false,
    val adresregel2: Boolean? = false,
    val adresregel3: Boolean? = false,
    val land: Boolean? = false,
    val indicatieVastgesteldVerblijftNietOpAdres: Boolean? = false,
    val aanhef: Boolean? = false,
    val aanschrijfwijze: Boolean? = false,
    val gebruikInLopendeTekst: Boolean? = false,
    val datumIngangOnderzoekPersoon: BrpDatum? = null,
    val datumIngangOnderzoekPartner: BrpDatum? = null,
    val datumIngangOnderzoekVerblijfplaats: BrpDatum? = null,
)