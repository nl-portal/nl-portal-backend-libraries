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
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductType
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.product.service.ProductService
import nl.nlportal.graphql.security.SecurityConstants
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.taak.domain.Taak
import java.util.*

class ProductQuery(
    private val productService: ProductService,
) : Query {
    @GraphQLDescription("Get list of products by product name")
    suspend fun getProducten(
        dfe: DataFetchingEnvironment,
        productName: String,
        pageNumber: Int? = 1,
        pageSize: Int? = 20,
    ): ProductPage {
        return productService.getProducten(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productName,
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
        )
    }

    @GraphQLDescription("Get product by id")
    suspend fun getProduct(
        dfe: DataFetchingEnvironment,
        id: UUID,
    ): Product {
        return productService.getProduct(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            id,
        )
    }

    @GraphQLDescription("Get list of zaken by product name ")
    suspend fun getProductZaken(
        dfe: DataFetchingEnvironment,
        productName: String,
        pageSize: Int? = 20,
    ): List<Zaak> {
        return productService.getProductZaken(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productName,
            pageNumber = 1,
        ).take(pageSize ?: 20)
    }

    @GraphQLDescription("Get list of taken by product name ")
    suspend fun getProductTaken(
        dfe: DataFetchingEnvironment,
        productName: String,
        pageSize: Int? = 20,
    ): List<Taak> {
        return productService.getProductTaken(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productName,
            pageNumber = 1,
            pageSize = pageSize ?: 20,
        ).take(pageSize ?: 20)
    }

    @GraphQLDescription("Get list of verbruiksobjecten of product ")
    suspend fun getProductVerbruiksObjecten(
        dfe: DataFetchingEnvironment,
        productId: UUID,
    ): List<ProductVerbruiksObject> {
        return productService.getProductVerbruiksObjecten(
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productId,
            pageNumber = 1,
            pageSize = 100,
        )
    }

    @GraphQLDescription("Get productType by name")
    suspend fun getProductType(productName: String): ProductType {
        return productService.getProductType(
            productName,
        )
    }
}