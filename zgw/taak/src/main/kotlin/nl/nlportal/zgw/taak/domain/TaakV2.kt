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

import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import java.time.LocalDateTime
import java.util.UUID

class TaakV2(
    val id: UUID,
    val titel: String,
    var status: TaakStatus,
    val soort: TaakSoort,
    val verloopdatum: LocalDateTime?,
    val identificatie: TaakIdentificatie,
    val koppeling: TaakKoppeling,
    val url: TaakUrl?,
    val portaalformulier: TaakForm?,
    val ogonebetaling: OgoneBetaling?,
    val eigenaar: String,
) {
    companion object {
        fun fromObjectsApi(objectsApiTask: ObjectsApiObject<TaakObjectV2>): TaakV2 {
            val taakObjectV2 = objectsApiTask.record.data
            return TaakV2(
                id = objectsApiTask.uuid,
                titel = taakObjectV2.titel,
                status = taakObjectV2.status,
                soort = taakObjectV2.soort,
                verloopdatum = taakObjectV2.verloopdatum,
                identificatie = taakObjectV2.identificatie,
                koppeling = taakObjectV2.koppeling,
                url = taakObjectV2.url,
                portaalformulier = taakObjectV2.portaalformulier,
                ogonebetaling = taakObjectV2.ogonebetaling,
                eigenaar = "gzac",
            )
        }
    }
}