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
package nl.nlportal.zgw.objectenapi.domain

data class UpdateObjectsApiObjectRequest<T>(
    val type: String,
    val record: UpdateObjectsApiObjectRequestRecord<T>
) {

    companion object {
        fun <T> fromObjectsApiObject(objectsApiObject: ObjectsApiObject<T>): UpdateObjectsApiObjectRequest<T> {
            return UpdateObjectsApiObjectRequest(
                objectsApiObject.type,
                UpdateObjectsApiObjectRequestRecord.fromObjectsApiObjectRecord(objectsApiObject.record)
            )
        }
    }
}

data class UpdateObjectsApiObjectRequestRecord<T>(
    val typeVersion: Int,
    val data: T,
    val startAt: String,
    var correctionFor: String? = null,
    var correctedBy: String? = null
) {

    companion object {
        fun <T> fromObjectsApiObjectRecord(objectsApiObjectRecord: ObjectsApiObjectRecord<T>): UpdateObjectsApiObjectRequestRecord<T> {
            return UpdateObjectsApiObjectRequestRecord(
                objectsApiObjectRecord.typeVersion,
                objectsApiObjectRecord.data,
                objectsApiObjectRecord.startAt
            )
        }
    }
}