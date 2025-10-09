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

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.graphql.domain.UpdateProductRequest
import nl.nlportal.openproduct.service.OpenProductService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class OpenProductMutation(
    val openProductService: OpenProductService,
) {
    @MutationMapping
    suspend fun updateProduct(
        authentication: CommonGroundAuthentication,
        @Argument productUpdateRequest: UpdateProductRequest,
    ): OpenProductProduct? =
        openProductService.updateProduct(
            authentication = authentication,
            productUpdate = productUpdateRequest.asOpenProductProductUpdate(),
        )
}