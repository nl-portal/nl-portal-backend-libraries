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

import java.util.UUID
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openproduct.client.domain.OpenProductProductType
import nl.nlportal.openproduct.client.domain.OpenProductProductTypeContent
import nl.nlportal.openproduct.service.OpenProductService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class OpenProductTypeQuery(
    val openProductService: OpenProductService,
) {
    @QueryMapping
    suspend fun getOpenProductTypes(
        authentication: CommonGroundAuthentication,
        @Argument pageNumber: Int? = null,
        @Argument pageSize: Int? = null,
        @Argument language: String? = null,
    ): ProductTypesPage =
        ProductTypesPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getProductTypes(
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                    language = language,
                ),
        )

    @QueryMapping
    suspend fun getOpenProductType(
        authentication: CommonGroundAuthentication,
        @Argument id: UUID,
        @Argument language: String? = null,
    ): OpenProductProductType? =
        openProductService.getProductType(
            id = id,
            language = language,
        )

    @SchemaMapping(typeName = "OpenProductProductType", field = "content")
    suspend fun content(
        openProductProductType: OpenProductProductType,
    ): List<OpenProductProductTypeContent>? = openProductService.getProductTypeContent(productTypeId = openProductProductType.uuid)
}