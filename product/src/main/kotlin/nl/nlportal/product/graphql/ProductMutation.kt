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
import com.expediagroup.graphql.server.operations.Mutation
import com.fasterxml.jackson.databind.node.ObjectNode
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.product.service.ProductService
import java.util.UUID

class ProductMutation(
    private val productService: ProductService,
) : Mutation {
    @GraphQLDescription("Update product verbruiks object")
    suspend fun updateProductVerbruiksObject(
        dfe: DataFetchingEnvironment,
        id: UUID,
        submission: ObjectNode,
    ): ProductVerbruiksObject {
        return productService.updateVerbruiksObject(
            id,
            submission,
            dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
        )
    }
}