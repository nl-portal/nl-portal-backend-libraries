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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import java.util.UUID

class Taak(
    val id: UUID,
    val title: String,
    val objectId: UUID,
    val identificatie: TaakIdentificatie,
    val formulier: TaakFormulier,
    val formId: String,
    val status: TaakStatus,
    val date: String,
    var data: ObjectNode
) {
    companion object {
        fun fromObjectsApiTask(objectsApiTask: ObjectsApiObject<TaakObject>): Taak {
            val taakObject = objectsApiTask.record.data
            return Taak(
                id = taakObject.verwerkerTaakId,
                title = taakObject.title,
                objectId = objectsApiTask.uuid,
                formulier = taakObject.formulier,
                status = taakObject.status,
                date = objectsApiTask.record.startAt,
                data = ObjectMapper().valueToTree(objectsApiTask.record.data.data),
                identificatie = taakObject.identificatie,
                formId = taakObject.formulier.value
            )
        }
    }
}