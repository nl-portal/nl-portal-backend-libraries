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
package nl.nlportal.zgw.taak.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.zgw.taak.domain.Taak
import nl.nlportal.zgw.taak.service.TaakService
import graphql.schema.DataFetchingEnvironment
import java.util.UUID

class TaakQuery(
    private val taskService: TaakService,
) : Query {

    @GraphQLDescription("Get a list of tasks")
    @Deprecated("Replaced by getTaken")
    suspend fun getTasks(
        dfe: DataFetchingEnvironment,
        pageNumber: Int? = 1,
        pageSize: Int? = 20,
    ): TaakPage {
        return getTaken(dfe, pageNumber, pageSize)
    }

    @GraphQLDescription("Get a list of tasks. Optional filter for zaak")
    suspend fun getTaken(
        dfe: DataFetchingEnvironment,
        pageNumber: Int? = 1,
        pageSize: Int? = 20,
        zaakUUID: UUID? = null,
    ): TaakPage {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)

        return taskService.getTaken(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            authentication = authentication,
            zaakUUID = zaakUUID,
        )
    }

    @GraphQLDescription("Get task by id")
    suspend fun getTaakById(id: UUID, dfe: DataFetchingEnvironment): Taak {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)
        return taskService.getTaakById(id, authentication)
    }
}