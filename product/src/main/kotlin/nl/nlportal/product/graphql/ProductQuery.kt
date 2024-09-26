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

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.core.util.Mapper
import nl.nlportal.graphql.security.SecurityConstants
import nl.nlportal.product.domain.DmnResponse
import nl.nlportal.product.domain.DmnVariable
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductType
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.product.service.DmnService
import nl.nlportal.product.service.ProductService
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.taak.domain.TaakV2
import java.util.*

class ProductQuery(
    private val productService: ProductService,
    private val dmnService: DmnService,
) : Query {
    @GraphQLDescription(
        """
        Get list of products by product name or productTypeId
        subProductType, is optional. It search for the subProductType in the products
        """,
    )
    suspend fun getProducten(
        dfe: DataFetchingEnvironment,
        productTypeId: UUID? = null,
        productName: String,
        subProductType: String? = null,
        pageNumber: Int? = 1,
        pageSize: Int? = 20,
    ): ProductPage {
        return productService.getProducten(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productTypeId,
            productName,
            subProductType,
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
        )
    }

    @GraphQLDescription("Get product by id")
    suspend fun getProduct(
        dfe: DataFetchingEnvironment,
        id: UUID,
    ): Product? {
        return productService.getProduct(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            id,
        )
    }

    @GraphQLDescription(
        """
        Get list of zaken by product name or productTypeId
        isOpen is optional, when not available, all zaken will be returned
        isOpen is true, only zaken without enddate will be returned
        isOpen is false, only zaken with an enddate will be returned
        """,
    )
    suspend fun getProductZaken(
        dfe: DataFetchingEnvironment,
        productTypeId: UUID? = null,
        productName: String,
        pageSize: Int? = 20,
        isOpen: Boolean? = null,
    ): List<Zaak> {
        return productService.getProductZaken(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productTypeId = productTypeId,
            productName = productName,
            pageNumber = 1,
            isOpen = isOpen,
        ).take(pageSize ?: 20)
    }

    @GraphQLDescription("Get list of taken by product name ")
    suspend fun getProductTaken(
        dfe: DataFetchingEnvironment,
        productTypeId: UUID? = null,
        productName: String,
        productSubType: String? = null,
        pageSize: Int? = 20,
    ): List<TaakV2> {
        return productService.getProductTaken(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productTypeId,
            productName,
            productSubType,
            pageNumber = 1,
            pageSize = pageSize ?: 20,
        ).take(pageSize ?: 20)
    }

    @GraphQLDescription("Get list of verbruiksobjecten of product")
    suspend fun getProductVerbruiksObjecten(productId: UUID): List<ProductVerbruiksObject> {
        return productService.getProductVerbruiksObjecten(
            productId.toString(),
            pageNumber = 1,
            pageSize = 100,
        )
    }

    @GraphQLDescription("Get productType by name")
    suspend fun getProductType(
        productTypeId: UUID? = null,
        productName: String,
    ): ProductType? {
        return productService.getProductType(
            productTypeId,
            productName,
        )
    }

    @GraphQLDescription("Get productTypes where the user has products")
    suspend fun getProductTypes(dfe: DataFetchingEnvironment): List<ProductType> {
        return productService.getProductTypes(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
        )
    }

    @GraphQLDescription(
        """
        Get Product Decision by key. Don't use it till it is configured in ProductType
        """,
    )
    suspend fun getProductDecision(
        sources: ObjectNode? = null,
        formulier: String,
        productTypeId: UUID? = null,
        productName: String,
        dmnVariables: ObjectNode? = null,
    ): List<DmnResponse> {
        var sourceMap: Map<String, UUID>? = null
        sources?.let { sourceMap = Mapper.get().convertValue(it, object : TypeReference<Map<String, UUID>>() {}) }
        return dmnService.getProductDecision(
            sources = sourceMap,
            formulier = formulier,
            productTypeId = productTypeId,
            productName = productName,
            dmnVariables = Mapper.get().convertValue(dmnVariables, object : TypeReference<Map<String, DmnVariable>>() {}),
        )
    }
}