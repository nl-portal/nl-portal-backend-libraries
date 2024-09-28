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
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.openklant.domain.Partij
import nl.nlportal.openklant.service.OpenKlant2Service

class PartijQuery(
    private val openklant2Service: OpenKlant2Service,
) : Query {
    @GraphQLDescription("Get user Partij")
    suspend fun getPartij(dfe: DataFetchingEnvironment): Partij? {
        return openklant2Service.findPartij(dfe.graphQlContext.get(AUTHENTICATION_KEY))
    }
}