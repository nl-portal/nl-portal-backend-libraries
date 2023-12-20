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

import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.case.domain.Case
import nl.nlportal.case.domain.CaseDefinitionId
import nl.nlportal.core.util.Mapper
import nl.nlportal.graphql.exception.UnauthorizedException
import nl.nlportal.task.BaseTest
import nl.nlportal.task.domain.Task
import nl.nlportal.task.domain.TaskId
import nl.nlportal.task.exception.UnauthorizedTaskException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
class CompletePublicTaskMutationTest : BaseTest() {
    lateinit var completePublicTaskMutation: CompletePublicTaskMutation

    @BeforeEach
    fun setup() {
        baseSetUp()
        completePublicTaskMutation = CompletePublicTaskMutation(taskService, caseService)
    }

    @Test
    fun `should complete public task`() {
        // test data
        val submission = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java)
        val taskId = UUID.randomUUID()
        val userId = "user"
        val externalCaseId = "externalCaseId"
        val case = Mockito.mock(Case::class.java)

        // set up mocks
        Mockito.`when`(taskService.completePublicTask(taskId.toString(), submission)).thenReturn(
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
        Mockito.`when`(caseService.getCase(externalCaseId)).thenReturn(case)
        Mockito.`when`(case.caseDefinitionId).thenReturn(CaseDefinitionId.existingId("caseDefinitionId"))

        // complete task
        val completedTask = completePublicTaskMutation.completePublicTask(taskId.toString(), submission)

        // verify
        verify(taskService).completePublicTask(taskId.toString(), submission)
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
    fun `should throw UnauthorizedException on UnauthorizedTaskException when completing public task`() {
        val taskId = UUID.randomUUID()
        val submission = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java)

        Mockito.`when`(taskService.completePublicTask(taskId.toString(), submission)).thenThrow(
            UnauthorizedTaskException(),
        )

        Assertions.assertThrows(UnauthorizedException::class.java) {
            completePublicTaskMutation.completePublicTask(taskId.toString(), submission)
        }
    }
}