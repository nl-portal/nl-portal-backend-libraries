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
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakV2
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class Product(
    var id: UUID?,
    @GraphQLIgnore
    @JsonProperty("PDCProductType")
    val productTypeId: String,
    val naam: String,
    @JsonProperty("subtype")
    val productSubType: String?,
    val status: String,
    val geldigVan: LocalDateTime,
    val geldigTot: LocalDateTime?,
    @GraphQLIgnore
    val rollen: Map<String, ProductRol>,
    val eigenschappen: ObjectNode?,
    val parameters: ObjectNode?,
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

    suspend fun productDetails(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): ProductDetails? {
        return id?.let { productService.getProductDetails(it) }
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
    ): List<TaakV2> {
        return taken.mapNotNull {
            productService.getObjectsApiObjectById<TaakObjectV2>(it.toString())
        }.map {
            TaakV2.fromObjectsApi(it)
        }
    }

    suspend fun verbruiksobjecten(
        @GraphQLIgnore
        @Autowired
        productService: ProductService,
    ): List<ProductVerbruiksObject> {
        return productService.getProductVerbruiksObjecten(
            productId = id.toString(),
            pageNumber = 1,
            pageSize = 100,
        )
    }

    companion object {
        fun fromObjectsApiProduct(objectsApiTask: ObjectsApiObject<Product>): Product {
            objectsApiTask.record.data.id = objectsApiTask.uuid
            return objectsApiTask.record.data
        }
    }
}