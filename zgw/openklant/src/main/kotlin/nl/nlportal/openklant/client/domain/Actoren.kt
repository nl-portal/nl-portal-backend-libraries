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
data class Actor(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val uuid: UUID? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val url: String? = null,
    val naam: String,
    val soortActor: SoortActor,
    val indicatieActief: Boolean? = null,
    val actoridentificator: OpenKlant2Identificator? = null,
) {
    init {
        require(naam == null || naam.length in 1..200) { "Actor name has to be between 1 and 200 characters long" }
    }
}

enum class SoortActor(
    @JsonValue private val value: String,
) {
    MEDEWERKER("medewerker"),
    GEAUTOMATISEERDE_ACTOR("geautomatiseerdeActor"),
    ORGANISATORISCHE_EENHEID("organisatorischeEenheid"),
    ;

    override fun toString(): String {
        return this.value
    }
}