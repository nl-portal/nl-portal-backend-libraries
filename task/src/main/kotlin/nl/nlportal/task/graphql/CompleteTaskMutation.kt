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
package nl.nlportal.task.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.case.service.CaseService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.graphql.exception.UnauthorizedException
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.task.exception.UnauthorizedTaskException
import nl.nlportal.task.service.TaskService
import graphql.schema.DataFetchingEnvironment
import java.util.UUID

class CompleteTaskMutation(
    private val taskService: TaskService,
    private val caseService: CaseService,
) : Mutation {
    @GraphQLDescription("Complete task mutation")
    fun completeTask(
        taskId: UUID,
        submission: ObjectNode,
        dfe: DataFetchingEnvironment,
    ): TaskInstance {
        try {
            val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)
            val completedTask = taskService.completeTask(taskId, submission, authentication.name)
            val caseInstance = caseService.getCase(completedTask.externalCaseId)!!
            return TaskInstance.from(completedTask, caseInstance.caseDefinitionId.value)
        } catch (ute: UnauthorizedTaskException) {
            throw UnauthorizedException()
        }
    }
}