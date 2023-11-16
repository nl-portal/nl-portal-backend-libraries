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
package nl.nlportal.task.service

import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.core.util.Mapper
import nl.nlportal.messaging.`in`.CreatePortalTaskMessage
import nl.nlportal.messaging.`in`.DeletePortalTaskMessage
import nl.nlportal.task.BaseIntegrationTest
import nl.nlportal.task.exception.UnauthorizedTaskException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

internal class TaskServiceIntTest : BaseIntegrationTest() {

    @Test
    @Transactional
    fun `should create a new portal task`() {
        val newTask = taskService.createPortalTask(createPortalTaskMessage())

        assertThat(newTask).isNotNull
        assertThat(newTask.createdOn).isBefore(LocalDateTime.now())
        assertThat(newTask.isCompleted()).isFalse
        assertThat(newTask.externalCaseId).isEqualTo("some-external-id")
    }

    @Test
    @Transactional
    fun `should create a new portal public task`() {
        val createPortalTaskMessage = CreatePortalTaskMessage(
            "some-task-id",
            "some-external-id",
            Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
            "some-task-def-key",
            true,
        )

        val newTask = taskService.createPortalTask(createPortalTaskMessage)

        assertThat(newTask.isPublic).isTrue
    }

    @Test
    @Transactional
    fun `should find all tasks for external case id`() {
        assertThat(taskService.findTasksForCase("some-external-id", "some-user-id")).isEmpty()
        taskService.createPortalTask(createPortalTaskMessage())
        assertThat(taskService.findTasksForCase("some-external-id", "some-user-id")).isNotEmpty
    }

    @Test
    @Transactional
    fun `should find all tasks`() {
        assertThat(taskService.findAllTasks("some-user-id")).isEmpty()
        taskService.createPortalTask(createPortalTaskMessage())
        assertThat(taskService.findAllTasks("some-user-id")).isNotEmpty
    }

    @Test
    @Transactional
    fun `should complete task`() {
        val dummyTask = taskService.createPortalTask(createPortalTaskMessage())

        val completedTask = taskService.completeTask(
            dummyTask.taskId.value,
            Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
            "some-user-id",
        )
        assertThat(completedTask.isCompleted()).isTrue
    }

    @Test
    @Transactional
    fun `should throw UnauthorizedTaskException when user not authorized when completing task`() {
        val dummyTask = taskService.createPortalTask(createPortalTaskMessage())

        Assertions.assertThrows(UnauthorizedTaskException::class.java) {
            taskService.completeTask(
                dummyTask.taskId.value,
                Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
                "some-other-user-id",
            )
        }
    }

    @Test
    @Transactional
    fun `should complete public task`() {
        val dummyTask = taskService.createPortalTask(createPortalTaskMessage(true))

        val completedTask = taskService.completePublicTask(
            dummyTask.externalTaskId,
            Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
        )
        assertThat(completedTask.isCompleted()).isTrue
    }

    @Test
    @Transactional
    fun `should throw UnauthorizedTaskException when completing public task but task is not public`() {
        val dummyTask = taskService.createPortalTask(createPortalTaskMessage())

        Assertions.assertThrows(UnauthorizedTaskException::class.java) {
            taskService.completePublicTask(
                dummyTask.externalTaskId,
                Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
            )
        }
    }

    @Test
    fun `should delete a public portal task`() {
        val task = taskService.createPortalTask(createPortalTaskMessage(true))
        assertThat(task).isNotNull
        assertThat(task.externalTaskId).isNotNull

        val deleteMessage = DeletePortalTaskMessage(
            task.externalTaskId,
        )
        taskService.deletePortalTask(deleteMessage)

        // Verify the deletion
        Assertions.assertThrows(EmptyResultDataAccessException::class.java) {
            taskRepository.findByTaskId(task.taskId)
        }
    }

    @Test
    fun `should delete a non-public portal task`() {
        val task = taskService.createPortalTask(createPortalTaskMessage())
        assertThat(task).isNotNull
        assertThat(task.externalTaskId).isNotNull

        val deleteMessage = DeletePortalTaskMessage(
            task.externalTaskId,
        )
        taskService.deletePortalTask(deleteMessage)

        // Verify the deletion
        Assertions.assertThrows(EmptyResultDataAccessException::class.java) {
            taskRepository.findByTaskId(task.taskId)
        }
    }
}