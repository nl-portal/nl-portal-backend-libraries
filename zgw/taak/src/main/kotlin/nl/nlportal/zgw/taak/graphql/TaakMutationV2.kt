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
import com.expediagroup.graphql.server.operations.Mutation
import com.fasterxml.jackson.databind.node.ObjectNode
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.domain.TaakVersion
import nl.nlportal.zgw.taak.service.TaakService
import java.util.UUID

class TaakMutationV2(
    private val taskService: TaakService,
) : Mutation {
    @GraphQLDescription("Submit a task")
    suspend fun submitTaakV2(
        dfe: DataFetchingEnvironment,
        id: UUID,
        submission: ObjectNode,
        version: TaakVersion,
    ): TaakV2 {
        return taskService.submitTaak(
            id,
            submission,
            dfe.graphQlContext[AUTHENTICATION_KEY],
            version,
        )
    }
}