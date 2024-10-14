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
package nl.nlportal.zgw.taak.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaakObjectV2(
    val titel: String,
    var status: TaakStatus,
    val soort: TaakSoort,
    val verloopdatum: LocalDateTime?,
    val identificatie: TaakIdentificatie,
    val koppeling: TaakKoppeling,
    val url: TaakUrl?,
    val portaalformulier: TaakForm?,
    val ogonebetaling: OgoneBetaling?,
    @JsonProperty("verwerker_taak_id") val verwerkerTaakId: UUID,
    val eigenaar: String,
)