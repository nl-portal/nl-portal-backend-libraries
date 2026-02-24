/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.graphql.domain

import java.time.LocalDate
import java.util.UUID
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdres
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdresUpdate

data class DigitaleAdresRequest(
    val uuid: UUID? = null,
    val waarde: String,
    val type: DigitaleAdresType,
    val omschrijving: String,
    val verificatieCode: String? = null,
    var verificatieDatum: LocalDate? = null,
) {
    fun asOpenKlant2DigitaleAdres(): OpenKlant2DigitaleAdres =
        OpenKlant2DigitaleAdres(
            adres = waarde,
            omschrijving = omschrijving,
            soortDigitaalAdres = type.name.lowercase(),
            verificatieDatum = verificatieDatum,
        )

    fun asOpenKlant2DigitaleAdresUpdate(): OpenKlant2DigitaleAdresUpdate =
        OpenKlant2DigitaleAdresUpdate(
            adres = waarde,
            omschrijving = omschrijving,
            soortDigitaalAdres = type.name.lowercase(),
            uuid = uuid!!,
            verificatieDatum = verificatieDatum,
        )
}