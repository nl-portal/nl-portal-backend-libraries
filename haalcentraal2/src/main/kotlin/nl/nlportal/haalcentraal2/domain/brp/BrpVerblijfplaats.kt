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

data class BrpVerblijfplaats(
    val type: String? = null,
    val functieAdres: BrpCodeOmschrijving? = null,
    val adresseerbaarObjectIdentificatie: String? = null,
    val nummeraanduidingIdentificatie: String? = null,
    val datumVan: BrpDatum? = null,
    val verblijfadres: Brp2Adres? = null,
    val indicatieVastgesteldVerblijftNietOpAdres: Boolean? = false,
    val inOnderzoek: BrpVerblijfplaatsInOnderzoek? = null,
)

data class BrpVerblijfplaatsInOnderzoek(
    val datumIngangOnderzoek: BrpDatum? = null,
    val type: Boolean? = false,
    val functieAdres: Boolean? = false,
    val adresseerbaarObjectIdentificatie: Boolean? = false,
    val nummeraanduidingIdentificatie: Boolean? = false,
    val datumVan: Boolean? = false,
    val verblijfplaats: Boolean? = false,
    val indicatieVastgesteldVerblijftNietOpAdres: Boolean? = false,
)