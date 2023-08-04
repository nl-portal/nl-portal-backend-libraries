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
package com.ritense.portal.taak.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ObjectsApiTaak(
    val identificatie: TaakIdentificatie,
    val data: Map<String, Any>,
    @JsonProperty("verzonden_data") var verzondenData: Map<String, Any>? = null,
    var status: TaakStatus,
    val formulier: TaakFormulier,
    @JsonProperty("verwerker_taak_id") val verwerkerTaakId: UUID,
)
data class TaakIdentificatie(
    val type: String,
    val value: String,
)

data class TaakFormulier (
    val type: String,
    val value: String
)