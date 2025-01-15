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
package nl.nlportal.openklant.client.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenKlant2DigitaleAdres(
    val adres: String,
    val omschrijving: String,
    val soortDigitaalAdres: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val url: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val uuid: UUID? = null,
    val verstrektDoorBetrokkene: OpenKlant2UUID? = null,
    val verstrektDoorPartij: OpenKlant2UUID? = null,
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

enum class OpenKlant2DigitaleAdressenFilters(
    @JsonValue val value: String,
) : OpenKlant2Filters {
    PAGE("page"),
    ;

    override fun toString() = this.value
}