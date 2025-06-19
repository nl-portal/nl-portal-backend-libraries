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
import nl.nlportal.openproduct.client.domain.OpenProductThema
import nl.nlportal.openproduct.graphql.domain.OpenProductThemaHierarchy
import nl.nlportal.openproduct.service.OpenProductService
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.taak.domain.TaakV2
import java.util.*

@AuthenticatedDirective
class OpenProductThemaQuery(
    val openProductService: OpenProductService,
) : Query {
    @GraphQLDescription("Get all themas")
    suspend fun getOpenProductThemas(
        pageNumber: Int? = null,
        pageSize: Int? = null,
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

    @GraphQLDescription("Get all hoofd themas")
    suspend fun getOpenProductHoofdThemas(): List<OpenProductThema> = openProductService.getHoofdThemas()

    @GraphQLDescription("Get all hoofd themas by producten")
    suspend fun getOpenProductHoofdThemasByProducten(
        dfe: DataFetchingEnvironment,
    ): List<OpenProductThema> =
        openProductService.getHoofdThemasByProducten(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
        )

    @GraphQLDescription("Get all themas hierarchy")
    suspend fun getOpenProductThemasHierarchy(): List<OpenProductThemaHierarchy> = openProductService.getThemasHierarchy()

    @GraphQLDescription("Get thema hierarchy")
    suspend fun getOpenProductThemaHierarchy(id: UUID): List<OpenProductThemaHierarchy> = openProductService.getThemaHierarchy(id = id)

    @GraphQLDescription("Get a thema")
    suspend fun getOpenProductThema(id: UUID): OpenProductThema? {
        val response =
            openProductService.getThema(
                id = id,
            )
        return response
    }

    @GraphQLDescription("Get zaken of a thema, including their hoofd themas")
    suspend fun getOpenProductThemaZaken(
        dfe: DataFetchingEnvironment,
        id: UUID,
        language: String? = null,
        isOpen: Boolean? = null,
    ): List<Zaak> =
        openProductService.getThemaZaken(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            id = id,
            language = language,
            isOpen = isOpen,
        )

    @GraphQLDescription("Get taken of a thema, including their hoofd themas")
    suspend fun getOpenProductThemaTaken(
        dfe: DataFetchingEnvironment,
        id: UUID,
        language: String? = null,
    ): List<TaakV2> =
        openProductService.getThemaTaken(
            authentication = dfe.graphQlContext[SecurityConstants.AUTHENTICATION_KEY],
            id = id,
            language = language,
        )
}