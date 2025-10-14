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
package nl.nlportal.product.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.LocalDateTime
import java.util.UUID
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject

class Product(
    var id: UUID?,
    @JsonProperty("PDCProductType")
    val productTypeId: String,
    val naam: String,
    @JsonProperty("subtype")
    val productSubType: String?,
    val status: String,
    val geldigVan: LocalDateTime,
    val geldigTot: LocalDateTime?,
    val rollen: Map<String, ProductRol>,
    val eigenschappen: ObjectNode?,
    val parameters: ObjectNode?,
    val zaken: List<UUID>,
    val documenten: List<String>,
) {
    companion object {
        fun fromObjectsApiProduct(objectsApiTask: ObjectsApiObject<Product>): Product {
            objectsApiTask.record.data.id = objectsApiTask.uuid
            return objectsApiTask.record.data
        }
    }
}