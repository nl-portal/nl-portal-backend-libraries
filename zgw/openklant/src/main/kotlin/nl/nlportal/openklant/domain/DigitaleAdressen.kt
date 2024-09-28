/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DigitaleAdres(
    val adres: String,
    val omschrijving: String,
    val soortDigitaalAdres: String,
    val url: String,
    val uuid: String,
    val verstrektDoorBetrokkene: OpenKlant2ForeignKey? = null,
    val verstrektDoorPartij: OpenKlant2ForeignKey? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CreateDigitaleAdres(
    val adres: String,
    val omschrijving: String,
    val soortDigitaalAdres: String,
    val verstrektDoorBetrokkene: OpenKlant2ForeignKey? = null,
    val verstrektDoorPartij: OpenKlant2ForeignKey? = null,
) {
    init {
        require(adres.length <= 80) {
            "adres can't be longer than 10 characters."
        }
        require(omschrijving.length <= 40) {
            "omschrijving can't be longer than 10 characters."
        }
        require(soortDigitaalAdres.length <= 255) {
            "soortDigitaalAdres can't be longer than 10 characters."
        }
    }
}