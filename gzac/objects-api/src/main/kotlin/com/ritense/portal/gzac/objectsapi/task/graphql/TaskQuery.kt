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
package com.ritense.portal.gzac.objectsapi.task.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import com.ritense.portal.gzac.objectsapi.task.domain.Task
import com.ritense.portal.gzac.objectsapi.task.service.TaskService
import graphql.schema.DataFetchingEnvironment
import java.util.UUID

class TaskQuery(
    private val taskService: TaskService
) : Query {

    @GraphQLDescription("Get a list of tasks")
    suspend fun getTasks(
        dfe: DataFetchingEnvironment,
        pageNumber: Int? = 1,
        pageSize: Int? = 20
    ): TaskPage {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)

        return taskService.getTasks(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            authentication = authentication
        )
    }

    @GraphQLDescription("Get task by id")
    suspend fun getTaskById(id: UUID, dfe: DataFetchingEnvironment): Task {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)
        return taskService.getTaskById(id, authentication)
    }
}