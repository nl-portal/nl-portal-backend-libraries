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
package nl.nlportal.haalcentraal.brp.domain.persoon

data class PersoonVerblijfplaats(
    val straat: String? = null,
    val huisnummer: String? = null,
    val huisletter: String? = null,
    val huisnummertoevoeging: String? = null,
    val postcode: String? = null,
    val woonplaats: String? = null,
    val adresseerbaarObjectIdentificatie: String?,
    val datumAanvangAdreshouding: PersoonDatum? = null,
    val datumIngangGeldigheid: PersoonDatum? = null,
    val datumInschrijvingInGemeente: PersoonDatum? = null,
    val datumVestigingInNederland: PersoonDatum? = null,
)