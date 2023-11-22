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
import nl.nlportal.case.service.CaseService
import nl.nlportal.messaging.`in`.CreatePortalTaskMessage
import nl.nlportal.messaging.`in`.DeletePortalTaskMessage
import nl.nlportal.messaging.out.CompleteTaskMessage
import nl.nlportal.messaging.out.PortalMessage
import nl.nlportal.task.domain.Task
import nl.nlportal.task.domain.TaskId
import nl.nlportal.task.exception.UnauthorizedTaskException
import nl.nlportal.task.repository.TaskRepository
import mu.KotlinLogging
import reactor.core.publisher.Sinks
import java.util.UUID

open class TaskService(
    private val taskRepository: TaskRepository,
    private val sink: Sinks.Many<PortalMessage>,
    private val caseService: CaseService,
) {

    fun createPortalTask(message: CreatePortalTaskMessage): Task {
        val case = caseService.getCase(message.externalCaseId)
        return taskRepository.save(
            Task(
                taskId = TaskId.newId(UUID.randomUUID()),
                externalTaskId = message.taskId,
                taskDefinitionKey = message.taskDefinitionKey,
                externalCaseId = message.externalCaseId,
                formDefinition = message.formDefinition,
                isPublic = message.isPublic,
                userId = case!!.userId,
            ),
        )
    }

    fun deletePortalTask(message: DeletePortalTaskMessage) {
        val task = taskRepository.findByExternalTaskId(message.taskId)
        if (task != null) {
            logger.info { "Deleting task with id: ${task.taskId}" }
            taskRepository.delete(task)
        } else {
            logger.debug { "Unable to find task with externalId: ${message.taskId}. Nothing is deleted" }
            // throw UnknownTaskException()
        }
    }

    fun findTasksForCase(externalCaseId: String, userId: String): List<Task>? {
        return taskRepository.findTasksByExternalCaseIdAndUserId(externalCaseId, userId)
    }

    fun findAllTasks(userId: String): List<Task> {
        return taskRepository.findAllByUserId(userId)
    }

    fun completeTask(id: UUID, submission: ObjectNode, userId: String?): Task {
        val task = taskRepository.findByTaskId(TaskId.existingId(id))
        if (task.userId.equals(userId)) {
            return completeTask(task, submission)
        } else {
            throw UnauthorizedTaskException()
        }
    }

    fun completePublicTask(taskExternalId: String, submission: ObjectNode): Task {
        val task = findPublicTask(taskExternalId)
        if (task.isPublic) {
            return completeTask(task, submission)
        } else {
            throw UnauthorizedTaskException()
        }
    }

    fun findPublicTask(taskId: String): Task {
        val task = taskRepository.findByExternalTaskId(taskId)
        if (task!!.isPublic) return task else throw UnauthorizedTaskException()
    }

    private fun completeTask(task: Task, submission: ObjectNode): Task {
        task.completeTask()
        val completedTask = taskRepository.save(task)
        sink.tryEmitNext(CompleteTaskMessage(completedTask.externalTaskId, completedTask.externalCaseId, submission))
        return completedTask
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}