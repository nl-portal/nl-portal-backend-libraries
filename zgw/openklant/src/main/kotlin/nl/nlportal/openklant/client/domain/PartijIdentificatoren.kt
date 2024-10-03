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

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

data class OpenKlant2PartijIdentificator(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val uuid: UUID? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val url: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val anderePartijIdentificator: String? = null,
    val identificeerdePartij: OpenKlant2IdentificeerdePartij? = null,
    val partijIdentificator: OpenKlant2Identificator? = null,
) {
    init {
        require(anderePartijIdentificator == null || anderePartijIdentificator.length <= 200) {
            "Andere partij indetificator can't be longer than 200 characters"
        }
    }
}

data class OpenKlant2IdentificeerdePartij(
    val uuid: UUID,
)