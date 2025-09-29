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
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.client.domain.OpenProductToegestaneStatus
import nl.nlportal.openproduct.service.OpenProductDmnService
import nl.nlportal.openproduct.service.OpenProductService
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.taak.domain.TaakV2
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class OpenProductQuery(
    val openProductService: OpenProductService,
    val openProductDmnService: OpenProductDmnService,
) {
    @QueryMapping
    suspend fun getOpenProducten(
        authentication: CommonGroundAuthentication,
        @Argument pageNumber: Int? = null,
        @Argument pageSize: Int? = null,
        @Argument status: String? = null,
    ): ProductenPage =
        ProductenPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getProducten(
                    authentication = authentication,
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                    status = status?.let { OpenProductToegestaneStatus.valueOf(status.uppercase()) },
                ),
        )

    @QueryMapping
    suspend fun getOpenProduct(
        authentication: CommonGroundAuthentication,
        @Argument id: UUID,
    ): OpenProductProduct? =
        openProductService.getProduct(
            authentication = authentication,
            id = id,
        )

    @QueryMapping
    suspend fun getOpenProductenByThema(
        authentication: CommonGroundAuthentication,
        @Argument themaId: UUID,
    ): List<OpenProductProduct> =
        openProductService.getProductenByThema(
            authentication = authentication,
            themaId = themaId,
        )

    @SchemaMapping(typeName = "OpenProductProduct", field = "zaken")
    suspend fun zaken(
        openProductProduct: OpenProductProduct,
    ): List<Zaak>? =
        openProductProduct.zaken?.let {
            openProductService.getProductZaken(
                it,
            )
        }

    @SchemaMapping(typeName = "OpenProductProduct", field = "taken")
    suspend fun taken(
        openProductProduct: OpenProductProduct,
    ): List<TaakV2>? =
        openProductProduct.taken?.let {
            openProductService.getProductTaken(
                it,
            )
        }

    @SchemaMapping(typeName = "OpenProductProduct", field = "acties")
    suspend fun acties(
        openProductProduct: OpenProductProduct,
    ): List<OpenProductActie> = openProductService.getProductActies(openProductProduct.producttype.uuid)

    @SchemaMapping(typeName = "OpenProductProduct", field = "decisions")
    suspend fun decisions(
        openProductProduct: OpenProductProduct,
    ): List<ObjectNode> {
        val result =
            openProductDmnService.getProductDecision(
                product = openProductProduct,
            )

        return Mapper.get().convertValue(result, object : TypeReference<List<ObjectNode>>() {})
    }
}
