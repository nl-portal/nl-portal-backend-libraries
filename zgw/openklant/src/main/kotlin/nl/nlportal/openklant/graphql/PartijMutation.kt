/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
import com.expediagroup.graphql.server.operations.Mutation
import com.fasterxml.jackson.databind.node.ObjectNode
import graphql.GraphQLException
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.openklant.domain.CreatePartij
import nl.nlportal.openklant.domain.Partij
import nl.nlportal.openklant.service.OpenKlant2Service

class PartijMutation(
    private val openklant2Service: OpenKlant2Service
) : Mutation {
    @GraphQLDescription("Create Partij for user")
    suspend fun createPartij(
        dfe: DataFetchingEnvironment,
        createPartijPayload: ObjectNode,
    ): Partij? {
        return openklant2Service.createPartij(dfe.graphQlContext.get(AUTHENTICATION_KEY), createPartijPayload)
    }

    @GraphQLDescription("Update user Partij")
    suspend fun updatePartij(
        dfe: DataFetchingEnvironment,
        partijPayload: ObjectNode,
    ): Partij {
        return openklant2Service.updatePartij(dfe.graphQlContext.get(AUTHENTICATION_KEY), partijPayload)
    }
}