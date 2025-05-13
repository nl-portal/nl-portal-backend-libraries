/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.federation.directives.AuthenticatedDirective
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.openproduct.client.domain.OpenProductProductType
import nl.nlportal.openproduct.service.OpenProductService
import java.util.*

@AuthenticatedDirective
class OpenProductTypeQuery(
    val openProductService: OpenProductService,
) : Query {
    @GraphQLDescription("Get all Open product types ")
    suspend fun getOpenProductTypes(
        pageNumber: Int? = null,
        pageSize: Int? = null,
        language: String,
    ): ProductTypesPage {
        return ProductTypesPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getProductTypes(
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                    language = language,
                ),
        )
    }

    @GraphQLDescription("Get a Open product type by id")
    suspend fun getOpenProductType(
        dfe: DataFetchingEnvironment,
        productTypeId: UUID,
        language: String,
    ): OpenProductProductType? {
        return openProductService.getProductType(
            productTypeId = productTypeId,
            language = language,
        )
    }
}