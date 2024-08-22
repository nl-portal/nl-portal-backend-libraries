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

import com.fasterxml.jackson.core.type.TypeReference
import nl.nlportal.core.util.Mapper
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
    val formtaak: TaakForm?,
    val ogonebetaling: OgoneBetaling?,
    val version: TaakVersion?,
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
                formtaak = taakObjectV2.formtaak,
                ogonebetaling = taakObjectV2.ogonebetaling,
                version = TaakVersion.V2,
                eigenaar = "gzac",
            )
        }

        fun migrate(taakV1: Taak): TaakV2 {
            return TaakV2(
                id = taakV1.id,
                titel = taakV1.title,
                status = taakV1.status,
                soort = TaakSoort.FORMTAAK,
                verloopdatum = taakV1.verloopdatum,
                identificatie = taakV1.identificatie,
                koppeling = TaakKoppeling.migrate(taakV1.zaak),
                url = null,
                formtaak =
                    TaakForm(
                        formulier = TaakFormulierV2.migrate(taakV1.formulier),
                        data = Mapper.get().convertValue(taakV1.data, object : TypeReference<Map<String, Any>>() {}),
                    ),
                ogonebetaling = null,
                version = TaakVersion.V1,
                eigenaar = "gzac",
            )
        }

        fun migrateObjectsApiTask(objectsApiTask: ObjectsApiObject<TaakObject>): TaakV2 {
            val taakObject = objectsApiTask.record.data
            return TaakV2(
                id = objectsApiTask.uuid,
                titel = taakObject.title,
                status = taakObject.status,
                soort = TaakSoort.FORMTAAK,
                verloopdatum = taakObject.verloopdatum,
                identificatie = taakObject.identificatie,
                koppeling =
                    TaakKoppeling.migrate(taakObject.zaak),
                url = null,
                formtaak =
                    TaakForm(
                        formulier = TaakFormulierV2.migrate(taakObject.formulier),
                        data = taakObject.data,
                    ),
                ogonebetaling = null,
                version = TaakVersion.V1,
                eigenaar = "gzac",
            )
        }
    }
}