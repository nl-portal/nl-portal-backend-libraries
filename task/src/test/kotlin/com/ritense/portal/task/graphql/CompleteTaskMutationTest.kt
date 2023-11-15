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
package com.ritense.portal.task.graphql

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.domain.Case
import com.ritense.portal.case.domain.CaseDefinitionId
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.graphql.exception.UnauthorizedException
import com.ritense.portal.graphql.security.SecurityConstants
import com.ritense.portal.task.BaseTest
import com.ritense.portal.task.domain.Task
import com.ritense.portal.task.domain.TaskId
import com.ritense.portal.task.exception.UnauthorizedTaskException
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
class CompleteTaskMutationTest : BaseTest() {

    lateinit var completeTaskMutation: CompleteTaskMutation

    var environment = mock(DataFetchingEnvironment::class.java)
    var authentication = mock(CommonGroundAuthentication::class.java)
    val context = mock(GraphQLContext::class.java)
    val userId = "user"

    @BeforeEach
    fun setup() {
        baseSetUp()
        `when`(authentication.name).thenReturn(userId)
        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(SecurityConstants.AUTHENTICATION_KEY)).thenReturn(authentication)
        completeTaskMutation = CompleteTaskMutation(taskService, caseService)
    }

    @Test
    fun `should complete task`() {
        // test data
        val submission = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java)
        val taskId = UUID.randomUUID()
        val externalCaseId = "externalCaseId"
        val case = mock(Case::class.java)

        // set up mocks
        `when`(taskService.completeTask(taskId, submission, userId)).thenReturn(
            Task(
                TaskId.existingId(taskId),
                "externalTaskId",
                "taskDefinitionKey",
                externalCaseId,
                userId,
                Mapper.get().createObjectNode(),
                true,
                LocalDateTime.now(),
                false,
            ),
        )
        `when`(caseService.getCase(externalCaseId)).thenReturn(case)
        `when`(case.caseDefinitionId).thenReturn(CaseDefinitionId.existingId("caseDefinitionId"))

        // complete task
        val completedTask = completeTaskMutation.completeTask(taskId, submission, environment)

        // verify
        verify(taskService).completeTask(taskId, submission, userId)
        verify(caseService).getCase(externalCaseId)

        assertEquals(taskId, completedTask.taskId)
        assertEquals("externalTaskId", completedTask.externalTaskId)
        assertEquals("externalCaseId", completedTask.externalCaseId)
        assertEquals("taskDefinitionKey", completedTask.taskDefinitionKey)
        assertEquals("caseDefinitionId", completedTask.caseDefinitionId)
        assertEquals(Mapper.get().createObjectNode(), completedTask.formDefinition)
        assertEquals(true, completedTask.isCompleted)
    }

    @Test
    fun `should throw UnauthorizedException on UnauthorizedTaskException when completing task`() {
        val taskId = UUID.randomUUID()
        val submission = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java)

        `when`(taskService.completeTask(taskId, submission, userId)).thenThrow(UnauthorizedTaskException())

        Assertions.assertThrows(UnauthorizedException::class.java) {
            completeTaskMutation.completeTask(taskId, submission, environment)
        }
    }
}