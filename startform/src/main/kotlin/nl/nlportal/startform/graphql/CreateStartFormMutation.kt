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
package nl.nlportal.startform.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.fasterxml.jackson.databind.node.ObjectNode
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.startform.service.StartFormService
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import java.util.*

class CreateStartFormMutation(private val startFormService: StartFormService) : Mutation {
    @GraphQLDescription("Save a start form to the objects api")
    suspend fun saveStartFormToObjectsApi(
        submission: ObjectNode,
        name: String,
        dfe: DataFetchingEnvironment,
    ): UUID {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)
        return startFormService.saveFormToObjectsApi(name, submission, authentication)
    }
}