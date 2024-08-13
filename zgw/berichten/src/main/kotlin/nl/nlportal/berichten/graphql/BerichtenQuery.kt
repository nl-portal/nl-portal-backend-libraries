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
package nl.nlportal.berichten.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.berichten.domain.Bericht
import nl.nlportal.berichten.service.BerichtenService
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import java.util.UUID

class BerichtenQuery(
    private val berichtenService: BerichtenService,
) : Query {
    @GraphQLDescription("Gets a single Bericht by Id")
    suspend fun getBericht(
        dfe: DataFetchingEnvironment,
        id: UUID,
    ): Bericht? {
        return berichtenService.getBericht(
            authentication = dfe.graphQlContext[AUTHENTICATION_KEY],
            id = id,
        )
    }

    @GraphQLDescription("Returns a paginated list of all Berichten")
    suspend fun getBerichten(
        dfe: DataFetchingEnvironment,
        pageNumber: Int? = 1,
        pageSize: Int? = 20,
    ): BerichtenPage {
        return berichtenService.getBerichtenPage(
            authentication = dfe.graphQlContext[AUTHENTICATION_KEY],
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
        )
    }

    @GraphQLDescription("Returns the total amount of unopened Berichten")
    suspend fun getUnopenedBerichtenCount(dfe: DataFetchingEnvironment): Int {
        return berichtenService
            .getUnopenedBerichtenCount(
                authentication = dfe.graphQlContext[AUTHENTICATION_KEY],
            )
    }
}