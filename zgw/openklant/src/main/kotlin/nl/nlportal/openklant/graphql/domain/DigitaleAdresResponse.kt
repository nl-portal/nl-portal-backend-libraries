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
import nl.nlportal.openklant.graphql.domain.DigitaleAdresType.ANDERS
import java.util.UUID

data class DigitaleAdresResponse(
    val uuid: UUID,
    val waarde: String,
    val type: DigitaleAdresType,
    val omschrijving: String,
) {
    companion object {
        fun fromOpenKlant2DigitaleAdres(openKlant2DigitaleAdres: OpenKlant2DigitaleAdres): DigitaleAdresResponse =
            DigitaleAdresResponse(
                uuid = openKlant2DigitaleAdres.uuid!!,
                waarde = openKlant2DigitaleAdres.adres,
                omschrijving = openKlant2DigitaleAdres.omschrijving,
                type =
                    DigitaleAdresType
                        .entries
                        .singleOrNull { it.name == openKlant2DigitaleAdres.soortDigitaalAdres }
                        ?: ANDERS,
            )
    }
}