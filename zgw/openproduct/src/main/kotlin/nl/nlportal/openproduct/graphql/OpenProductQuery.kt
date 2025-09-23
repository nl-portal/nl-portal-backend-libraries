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
import nl.nlportal.graphql.security.SecurityConstants
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.client.domain.OpenProductToegestaneStatus
import nl.nlportal.openproduct.service.OpenProductService
import java.util.UUID

@AuthenticatedDirective
class OpenProductQuery(
    val openProductService: OpenProductService,
) : Query {
    @GraphQLDescription(
        """
        Get all Open producten
        The allowed statussen:
        - initieel
        - gereed
        - actief
        - ingetrokken
        - geweigerd
        - verlopen
    """,
    )
    suspend fun getOpenProducten(
        dfe: DataFetchingEnvironment,
        pageNumber: Int? = null,
        pageSize: Int? = null,
        status: String? = null,
        productTypeCode: String? = null,
        productTypeId: String? = null,
    ): ProductenPage =
        ProductenPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getProducten(
                    authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                    status = status?.let { OpenProductToegestaneStatus.valueOf(status.uppercase()) },
                    productTypeCode = productTypeCode,
                    productTypeId = productTypeId,
                ),
        )

    @GraphQLDescription("Get a Open product type by id")
    suspend fun getOpenProduct(
        dfe: DataFetchingEnvironment,
        id: UUID,
    ): OpenProductProduct? =
        openProductService.getProduct(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            id = id,
        )

    @GraphQLDescription("Get a Open producten type by thema id")
    suspend fun getOpenProductenByThema(
        dfe: DataFetchingEnvironment,
        themaId: UUID,
    ): List<OpenProductProduct> =
        openProductService.getProductenByThema(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            themaId = themaId,
        )
}