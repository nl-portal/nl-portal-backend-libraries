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

import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdres
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdresUpdate
import java.time.LocalDate
import java.util.UUID

data class DigitaleAdresRequest(
    val uuid: UUID? = null,
    val waarde: String,
    val type: DigitaleAdresType,
    val omschrijving: String,
    val isGeverifieerd: Boolean? = false,
) {
    fun asOpenKlant2DigitaleAdres(): OpenKlant2DigitaleAdres =
        OpenKlant2DigitaleAdres(
            adres = waarde,
            omschrijving = omschrijving,
            soortDigitaalAdres = type.name.lowercase(),
            verificatieDatum =
                when (isGeverifieerd) {
                    true -> {
                        LocalDate.now()
                    }
                    else -> {
                        null
                    }
                },
        )

    fun asOpenKlant2DigitaleAdresUpdate(): OpenKlant2DigitaleAdresUpdate =
        OpenKlant2DigitaleAdresUpdate(
            adres = waarde,
            omschrijving = omschrijving,
            soortDigitaalAdres = type.name.lowercase(),
            uuid = uuid!!,
            verificatieDatum =
                when (isGeverifieerd) {
                    true -> {
                        LocalDate.now()
                    }
                    else -> {
                        null
                    }
                },
        )
}