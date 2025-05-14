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
import com.expediagroup.graphql.server.operations.Mutation
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.graphql.domain.UpdateProductRequest
import nl.nlportal.openproduct.service.OpenProductService

@AuthenticatedDirective
class OpenProductMutation(
    val openProductService: OpenProductService,
) : Mutation {
    @GraphQLDescription("Update product")
    suspend fun updateProduct(
        dfe: DataFetchingEnvironment,
        productUpdateRequest: UpdateProductRequest,
    ): OpenProductProduct? {
        return openProductService.updateProduct(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            productUpdate = productUpdateRequest.asOpenProductProductUpdate(),
        )
    }
}