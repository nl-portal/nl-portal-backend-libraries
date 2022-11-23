/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.task.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.case.domain.CaseId
import com.ritense.portal.case.service.CaseService
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import com.ritense.portal.task.service.TaskService
import graphql.schema.DataFetchingEnvironment
import org.springframework.security.core.Authentication
import java.util.UUID

class TaskQuery(
    private val taskService: TaskService,
    private val caseService: CaseService
) : Query {

    @GraphQLDescription("find all available tasks for external case id")
    fun findTasks(caseId: UUID, dfe: DataFetchingEnvironment): List<TaskInstance>? {
        val authentication = dfe.graphQlContext.get<Authentication>(AUTHENTICATION_KEY)
        val case = caseService.getCase(CaseId.existingId(caseId), authentication.name) ?: return null
        return taskService.findTasksForCase(case.externalId!!, authentication.name)?.sortedByDescending { it.createdOn }
            ?.map { TaskInstance.from(it, case.caseDefinitionId.value) }
    }

    @GraphQLDescription("find all available tasks")
    fun findAllTasks(dfe: DataFetchingEnvironment): List<TaskInstance> {
        val authentication = dfe.graphQlContext.get<Authentication>(AUTHENTICATION_KEY)
        return taskService.findAllTasks(authentication.name)
            .map {
                val case = caseService.getCase(it.externalCaseId)!!
                TaskInstance.from(it, case.caseDefinitionId.value)
            }
            .sortedByDescending { it.createdOn }
    }
}