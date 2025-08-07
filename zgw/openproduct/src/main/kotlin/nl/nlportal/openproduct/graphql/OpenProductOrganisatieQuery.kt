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
import nl.nlportal.openproduct.client.domain.OpenProductOrganisatie
import nl.nlportal.openproduct.service.OpenProductService
import java.util.UUID

@AuthenticatedDirective
class OpenProductOrganisatieQuery(
    val openProductService: OpenProductService,
) : Query {
    @GraphQLDescription("Get all organisaties")
    suspend fun getOpenProductOrganisaties(
        pageNumber: Int? = null,
        pageSize: Int? = null,
        naam: String? = null,
    ): OrganisatiesPage =
        OrganisatiesPage.fromResultPage(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            resultPage =
                openProductService.getOrganisaties(
                    pageNumber = pageNumber ?: 1,
                    pageSize = pageSize ?: 20,
                    naam = naam,
                ),
        )

    @GraphQLDescription("Get a organisatie")
    suspend fun getOpenProductOrganisatie(id: UUID): OpenProductOrganisatie? =
        openProductService.getOrganisatie(
            id = id,
        )
}