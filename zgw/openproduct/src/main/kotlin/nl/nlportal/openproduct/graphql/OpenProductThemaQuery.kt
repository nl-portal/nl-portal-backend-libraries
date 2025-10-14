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
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.client.domain.OpenProductThema
import nl.nlportal.openproduct.graphql.domain.OpenProductThemaHierarchy
import nl.nlportal.openproduct.service.OpenProductService
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.taak.domain.TaakV2
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class OpenProductThemaQuery(
    val openProductService: OpenProductService,
) {
    @QueryMapping
    suspend fun getOpenProductThemas(
        @Argument pageNumber: Int? = null,
        @Argument pageSize: Int? = null,
    ): ThemasPage =
        ThemasPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getThemas(
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                ),
        )

    @QueryMapping
    suspend fun getOpenProductHoofdThemas(): List<OpenProductThema> = openProductService.getHoofdThemas()

    @QueryMapping
    suspend fun getOpenProductHoofdThemasByProducten(
        authentication: CommonGroundAuthentication,
    ): List<OpenProductThema> =
        openProductService.getHoofdThemasByProducten(
            authentication = authentication,
        )

    @QueryMapping
    suspend fun getOpenProductThemasHierarchy(): List<OpenProductThemaHierarchy> = openProductService.getThemasHierarchy()

    @QueryMapping
    suspend fun getOpenProductThemaHierarchy(
        @Argument id: UUID,
    ): List<OpenProductThemaHierarchy> = openProductService.getThemaHierarchy(id = id)

    @QueryMapping
    suspend fun getOpenProductThema(
        @Argument id: UUID,
    ): OpenProductThema? {
        val response =
            openProductService.getThema(
                id = id,
            )
        return response
    }

    @QueryMapping
    suspend fun getOpenProductThemaZaken(
        authentication: CommonGroundAuthentication,
        @Argument id: UUID,
        @Argument language: String? = null,
        @Argument isOpen: Boolean? = null,
        @Argument pageSize: Int? = null,
    ): List<Zaak> =
        openProductService.getThemaZaken(
            authentication = authentication,
            id = id,
            language = language,
            isOpen = isOpen,
            pageSize = pageSize,
        )

    @QueryMapping
    suspend fun getOpenProductThemaTaken(
        authentication: CommonGroundAuthentication,
        @Argument id: UUID,
        @Argument language: String? = null,
        @Argument pageSize: Int? = null,
    ): List<TaakV2> =
        openProductService.getThemaTaken(
            authentication = authentication,
            id = id,
            language = language,
            pageSize = pageSize,
        )

    @SchemaMapping(typeName = "OpenProductThema", field = "zaken")
    suspend fun zaken(
        authentication: CommonGroundAuthentication,
        openProductThema: OpenProductThema,
    ): List<Zaak>? =
        openProductService.getThemaZaken(
            authentication = authentication,
            id = openProductThema.uuid,
        )

    @SchemaMapping(typeName = "OpenProductThema", field = "taken")
    suspend fun taken(
        authentication: CommonGroundAuthentication,
        openProductThema: OpenProductThema,
    ): List<TaakV2>? =
        openProductService.getThemaTaken(
            authentication = authentication,
            id = openProductThema.uuid,
        )

    @SchemaMapping(typeName = "OpenProductThema", field = "producten")
    suspend fun producten(
        authentication: CommonGroundAuthentication,
        openProductThema: OpenProductThema,
    ): List<OpenProductProduct>? =
        openProductService.getThemaProducten(
            authentication = authentication,
            thema = openProductThema,
        )
}