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

import nl.nlportal.case.domain.Case
import nl.nlportal.case.domain.CaseDefinitionId
import nl.nlportal.case.domain.CaseId
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.task.BaseTest
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
internal class TaskQueryTest : BaseTest() {
    lateinit var taskQuery: TaskQuery

    @Mock
    lateinit var authentication: Authentication

    @Mock
    lateinit var environment: DataFetchingEnvironment

    @Mock
    lateinit var context: GraphQLContext

    @BeforeEach
    fun setup() {
        baseSetUp()
        taskQuery = TaskQuery(taskService, caseService)

        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
        `when`(authentication.name).thenReturn("userId")
    }

    @Test
    fun `should find task for case`() {
        val caseId = UUID.randomUUID()

        val case = mock(Case::class.java)
        val taskCreatedToday = task(UUID.randomUUID(), false, LocalDateTime.now(), "userId")
        val taskCreatedYesterday = task(UUID.randomUUID(), false, LocalDateTime.now().minusDays(1), "userId")

        `when`(case.caseDefinitionId).thenReturn(CaseDefinitionId.existingId("caseDefinitionId"))
        `when`(case.externalId).thenReturn("externalId")

        `when`(caseService.getCase(CaseId.existingId(caseId), "userId")).thenReturn(case)
        `when`(taskService.findTasksForCase("externalId", "userId")).thenReturn(
            listOf(taskCreatedYesterday, taskCreatedToday),
        )

        val tasks = taskQuery.findTasks(caseId, environment)

        verify(caseService, times(1)).getCase(CaseId.existingId(caseId), "userId")
        verify(taskService, times(1)).findTasksForCase("externalId", "userId")

        // test order in decending based on createdOn
        assertEquals(taskCreatedToday.taskId.value, tasks?.get(0)?.taskId)
        assertEquals(taskCreatedYesterday.taskId.value, tasks?.get(1)?.taskId)
    }

    @Test
    fun `should find all tasks`() {
        val case = mock(Case::class.java)
        val taskCreatedToday = task(UUID.randomUUID(), false, LocalDateTime.now(), "userId")
        val taskCreatedYesterday = task(UUID.randomUUID(), false, LocalDateTime.now().minusDays(1), "userId")

        `when`(case.caseDefinitionId).thenReturn(CaseDefinitionId.existingId("caseDefinitionId"))

        `when`(caseService.getCase("externalCaseId")).thenReturn(case)
        `when`(taskService.findAllTasks("userId")).thenReturn(
            listOf(taskCreatedYesterday, taskCreatedToday),
        )

        val tasks = taskQuery.findAllTasks(environment)

        verify(taskService, times(1)).findAllTasks("userId")
        verify(caseService, times(2)).getCase("externalCaseId")

        // test order in decending based on createdOn
        assertEquals(taskCreatedToday.taskId.value, tasks.get(0).taskId)
        assertEquals(taskCreatedYesterday.taskId.value, tasks.get(1).taskId)
    }
}