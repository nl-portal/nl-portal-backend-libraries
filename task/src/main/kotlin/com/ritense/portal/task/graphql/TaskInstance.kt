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
import com.ritense.portal.task.domain.Task
import java.time.format.DateTimeFormatter
import java.util.UUID

data class TaskInstance(
    val taskId: UUID,
    val externalTaskId: String,
    val externalCaseId: String,
    val taskDefinitionKey: String,
    val caseDefinitionId: String? = null,
    val formDefinition: ObjectNode,
    val isCompleted: Boolean,
    val createdOn: String,
) {
    companion object {
        fun from(task: Task, caseDefinitionId: String? = null): TaskInstance {
            return TaskInstance(
                task.id.value,
                task.externalTaskId,
                task.externalCaseId,
                task.taskDefinitionKey,
                caseDefinitionId,
                task.formDefinition,
                task.isCompleted(),
                task.createdOn.format(DateTimeFormatter.ISO_DATE_TIME),
            )
        }
    }
}