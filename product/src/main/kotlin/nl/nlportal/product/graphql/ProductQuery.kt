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
package nl.nlportal.product.graphql

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.UUID
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.product.domain.DmnVariable
import nl.nlportal.product.domain.PrefillResponse
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductDetails
import nl.nlportal.product.domain.ProductType
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.product.service.DmnService
import nl.nlportal.product.service.PrefillService
import nl.nlportal.product.service.ProductService
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.taak.domain.TaakV2
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ProductQuery(
    private val productService: ProductService,
    private val dmnService: DmnService,
    private val prefillService: PrefillService,
) {
    @QueryMapping
    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
        @Argument subProductType: String? = null,
        @Argument pageNumber: Int? = 1,
        @Argument pageSize: Int? = 20,
    ): ProductPage =
        productService.getProducten(
            authentication = authentication,
            productTypeId,
            productName,
            subProductType,
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
        )

    @QueryMapping
    suspend fun getProduct(
        authentication: CommonGroundAuthentication,
        @Argument id: UUID,
    ): Product? =
        productService.getProduct(
            authentication = authentication,
            id = id,
        )

    @QueryMapping
    suspend fun getProductZaken(
        authentication: CommonGroundAuthentication,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
        @Argument pageSize: Int? = null,
        @Argument isOpen: Boolean? = null,
    ): List<Zaak> =
        productService.getProductZaken(
            authentication = authentication,
            productTypeId = productTypeId,
            productName = productName,
            pageNumber = 1,
            isOpen = isOpen,
            pageSize = pageSize,
        )

    @QueryMapping
    suspend fun getProductTaken(
        authentication: CommonGroundAuthentication,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
        @Argument productSubType: String? = null,
        @Argument pageSize: Int? = 20,
    ): List<TaakV2> =
        productService
            .getProductTaken(
                authentication = authentication,
                productTypeId,
                productName,
                productSubType,
                pageNumber = 1,
                pageSize = pageSize ?: 20,
            ).take(pageSize ?: 20)

    @QueryMapping
    suspend fun getProductVerbruiksObjecten(
        authentication: CommonGroundAuthentication,
        @Argument productId: UUID,
    ): List<ProductVerbruiksObject> =
        productService.getProductVerbruiksObjecten(
            productId.toString(),
            pageNumber = 1,
            pageSize = 20,
        )

    @QueryMapping
    suspend fun getProductType(
        authentication: CommonGroundAuthentication,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
    ): ProductType? =
        productService.getProductType(
            productTypeId,
            productName,
        )

    @QueryMapping
    suspend fun getProductTypes(authentication: CommonGroundAuthentication): List<ProductType> =
        productService.getProductTypes(
            authentication = authentication,
        )

    @QueryMapping
    suspend fun getProductDecision(
        authentication: CommonGroundAuthentication,
        @Argument sources: Any? = null,
        @Argument key: String,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
        @Argument dmnVariables: Any? = null,
    ): List<ObjectNode> {
        val result =
            dmnService.getProductDecision(
                sources = sources?.let { Mapper.get().convertValue(it, object : TypeReference<Map<String, UUID>>() {}) },
                key = key,
                productTypeId = productTypeId,
                productName = productName,
                dmnVariables =
                    dmnVariables?.let {
                        Mapper.get().convertValue(
                            dmnVariables,
                            object : TypeReference<Map<String, DmnVariable>>() {},
                        )
                    },
            )

        return Mapper.get().convertValue(result, object : TypeReference<List<ObjectNode>>() {})
    }

    @QueryMapping
    suspend fun getDecision(
        authentication: CommonGroundAuthentication,
        @Argument sources: Any? = null,
        @Argument key: String,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
        @Argument dmnVariables: Any? = null,
    ): List<ObjectNode> {
        val result =
            dmnService.getDecision(
                sources = sources?.let { Mapper.get().convertValue(it, object : TypeReference<Map<String, String>>() {}) },
                key = key,
                productTypeId = productTypeId,
                productName = productName,
                dmnVariables =
                    dmnVariables?.let {
                        Mapper.get().convertValue(
                            dmnVariables,
                            object : TypeReference<Map<String, DmnVariable>>() {},
                        )
                    },
            )

        return Mapper.get().convertValue(result, object : TypeReference<List<ObjectNode>>() {})
    }

    @QueryMapping
    suspend fun productPrefill(
        @Argument sources: Any? = null,
        @Argument staticData: Any? = null,
        @Argument productTypeId: UUID? = null,
        @Argument productName: String,
        @Argument key: String,
        authentication: CommonGroundAuthentication,
    ): PrefillResponse =
        prefillService.prefill(
            sources = sources?.let { Mapper.get().convertValue(it, object : TypeReference<Map<String, UUID>>() {}) },
            staticData = staticData?.let { Mapper.get().convertValue(it, object : TypeReference<Map<String, Any>>() {}) },
            productTypeId = productTypeId,
            productName = productName,
            key = key,
            authentication = authentication,
        )

    @SchemaMapping(typeName = "Product", field = "productType")
    suspend fun productType(
        product: Product,
    ): ProductType? = productService.getObjectsApiObjectById<ProductType>(product.productTypeId)?.record?.data

    @SchemaMapping(typeName = "Product", field = "productDetails")
    suspend fun productDetails(
        product: Product,
    ): ProductDetails? = product.id?.let { productService.getProductDetails(it) }

    @SchemaMapping(typeName = "Product", field = "zaken")
    suspend fun zaken(
        product: Product,
    ): List<Zaak> = product.zaken.map { productService.getZaak(it) }

    @SchemaMapping(typeName = "Product", field = "taken")
    suspend fun taken(
        product: Product,
        authentication: CommonGroundAuthentication,
    ): List<TaakV2> =
        productService.getTaken(
            authentication = authentication,
            productId = product.id!!,
            product.zaken,
        )

    @SchemaMapping(typeName = "Product", field = "verbruiksobjecten")
    suspend fun verbruiksobjecten(
        product: Product,
    ): List<ProductVerbruiksObject> =
        productService.getProductVerbruiksObjecten(
            productId = product.id.toString(),
            pageNumber = 1,
            pageSize = 20,
        )

    @SchemaMapping(typeName = "ProductType", field = "beslistabelMappings")
    fun beslistabelMappings(
        productType: ProductType,
    ): List<String>? = productType.beslistabelmapping?.map { it.key }

    @SchemaMapping(typeName = "ProductType", field = "prefillMappings")
    fun prefillMappings(
        productType: ProductType,
    ): ObjectNode? {
        val prefillMap = mutableMapOf<String, Set<String>>()
        productType.prefillmapping?.forEach {
            prefillMap[it.key] = it.value.variabelen.keys
        }

        return Mapper.get().convertValue(prefillMap, object : TypeReference<ObjectNode>() {})
    }
}