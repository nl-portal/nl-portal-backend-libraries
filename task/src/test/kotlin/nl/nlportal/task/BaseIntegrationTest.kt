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
package nl.nlportal.task

import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.case.domain.Case
import nl.nlportal.case.domain.CaseDefinitionId
import nl.nlportal.case.domain.CaseId
import nl.nlportal.case.domain.Status
import nl.nlportal.case.domain.Submission
import nl.nlportal.case.service.CaseService
import nl.nlportal.core.util.Mapper
import nl.nlportal.messaging.`in`.CreatePortalTaskMessage
import nl.nlportal.task.repository.TaskRepository
import nl.nlportal.task.service.TaskService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var taskRepository: TaskRepository

    @MockBean
    lateinit var caseService: CaseService

    lateinit var case: Case

    @BeforeEach
    fun setUp() {
        case = Case(
            caseId = CaseId.existingId(UUID.randomUUID()),
            userId = "some-user-id",
            externalId = "some-external-id",
            status = Status("a"),
            caseDefinitionId = CaseDefinitionId.existingId("person"),
            submission = Submission(
                Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
            ),
        )

        `when`(caseService.getCase("some-external-id")).thenReturn(case)
    }

    protected fun createPortalTaskMessage(isPublic: Boolean = false): CreatePortalTaskMessage {
        return CreatePortalTaskMessage(
            "some-task-id",
            "some-external-id",
            Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
            "some-task-def-key",
            isPublic,
        )
    }

    @AfterEach
    fun afterEach() {
    }
}