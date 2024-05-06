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

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.product.service.ProductService
import nl.nlportal.core.util.Mapper
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.taak.domain.Taak
import nl.nlportal.zgw.taak.domain.TaakObject
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID

class Product(
    val id: UUID,
    @GraphQLIgnore
    @JsonProperty("PDCProductType")
    val productTypeId: String,
    val naam: String,
    val status: String,
    val geldigVan: LocalDate,
    val geldigTot: LocalDate?,
    @GraphQLIgnore
    val rollen: Map<String, ProductRol>,
    val eigenschappen: ObjectNode,
    @GraphQLIgnore
    val verbruiksobjecten: Map<String, String>,
    @GraphQLIgnore
    val taken: List<UUID>,
    @GraphQLIgnore
    val zaken: List<UUID>,
    val documenten: List<String>,
) {
    suspend fun productType(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): ProductType? {
        return productService.getObjectsApiObjectById<ProductType>(productTypeId)?.record?.data
    }

    suspend fun verbruiksobjecten(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): ObjectNode {
        val verbruiksobjectMap = mutableMapOf<String, ProductVerbruiksObject?>()
        verbruiksobjecten.mapNotNull {
            val verbuiksObject = productService.getObjectsApiObjectById<ProductVerbruiksObject>(it.value)
            if (verbuiksObject != null) {
                verbruiksobjectMap[it.key] = verbuiksObject.record.data
            }
        }
        return Mapper.get().convertValue(verbruiksobjectMap, ObjectNode::class.java)
    }

    suspend fun productDetails(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): ProductDetails? {
        return productService.getProductDetails(id)
    }

    suspend fun zaken(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): List<Zaak> {
        return zaken.map { productService.getZaak(it) }
    }

    suspend fun taken(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): List<Taak> {
        val takenList = mutableListOf<Taak>()
        taken.forEach {
            val objectsApiTask = productService.getObjectsApiObjectById<TaakObject>(it.toString())
            if (objectsApiTask != null) {
                takenList.add(Taak.fromObjectsApiTask(objectsApiTask))
            }
        }
        return takenList
    }

    companion object {
        fun fromObjectsApiProduct(objectsApiTask: ObjectsApiObject<Product>): Product {
            return objectsApiTask.record.data
        }
    }
}