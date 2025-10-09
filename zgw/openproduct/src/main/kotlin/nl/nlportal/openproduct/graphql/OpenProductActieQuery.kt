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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.UUID
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.openproduct.client.domain.OpenProductActie
import nl.nlportal.openproduct.service.OpenProductDmnService
import nl.nlportal.openproduct.service.OpenProductService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class OpenProductActieQuery(
    val openProductService: OpenProductService,
    val openProductDmnService: OpenProductDmnService,
) {
    @QueryMapping
    suspend fun getOpenProductActies(
        @Argument pageNumber: Int? = null,
        @Argument pageSize: Int? = null,
    ): ActiesPage =
        ActiesPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getActies(
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                ),
        )

    @QueryMapping
    suspend fun getOpenProductActie(
        @Argument id: UUID,
    ): OpenProductActie? =
        openProductService.getActie(
            id = id,
        )

    @QueryMapping
    suspend fun getOpenProductActieDecision(
        authentication: CommonGroundAuthentication,
        @Argument productId: UUID,
        @Argument naam: String,
    ): List<ObjectNode> {
        val result =
            openProductDmnService.getActieDecision(
                authentication = authentication,
                naam = naam,
                productId = productId,
            )

        return Mapper.get().convertValue(result, object : TypeReference<List<ObjectNode>>() {})
    }
}