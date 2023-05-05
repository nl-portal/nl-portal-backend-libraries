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
package com.ritense.portal.task

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.service.CaseDefinitionService
import com.ritense.portal.case.service.CaseService
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.task.domain.Task
import com.ritense.portal.task.domain.TaskId
import com.ritense.portal.task.service.TaskService
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import java.util.UUID

abstract class BaseTest {

    @Mock
    lateinit var caseService: CaseService

    @Mock
    lateinit var taskService: TaskService

    @Mock
    lateinit var caseDefinitionService: CaseDefinitionService

    fun baseSetUp() {
        MockitoAnnotations.openMocks(this)
    }

    fun task(taskId: UUID, isPublic: Boolean, createdOn: LocalDateTime, userId: String): Task {
        return Task(
            taskId = TaskId.newId(taskId),
            externalCaseId = "externalCaseId",
            externalTaskId = "externalTaskId",
            formDefinition = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
            taskDefinitionKey = "taskDefinitionKey",
            userId = userId,
            isPublic = isPublic,
            createdOn = createdOn
        )
    }
}