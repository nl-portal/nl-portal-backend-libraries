/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.service.OpenKlant2Service
import java.util.UUID

class PartijQuery(
    private val openklant2Service: OpenKlant2Service,
) : Query {
    @GraphQLDescription("Find the Partij of the authenticated user.")
    suspend fun findUserPartij(dfe: DataFetchingEnvironment): OpenKlant2Partij? {
        return openklant2Service.findPartij(dfe.graphQlContext.get(AUTHENTICATION_KEY))
    }

    @GraphQLDescription("Get Partij by Id for authenticated user.")
    suspend fun getUserPartij(
        dfe: DataFetchingEnvironment,
        partijId: UUID,
    ): OpenKlant2Partij? {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)
        val userPartijen =
            openklant2Service
                .findPartijIdentificatoren(authentication)
                ?.mapNotNull { it.identificeerdePartij?.uuid }

        if (userPartijen == null || partijId !in userPartijen) return null
        val partijResponse = openklant2Service.getPartij(partijId = partijId)

        return partijResponse
    }
}