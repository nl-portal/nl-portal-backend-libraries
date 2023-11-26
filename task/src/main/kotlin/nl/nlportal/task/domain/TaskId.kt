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
package nl.nlportal.task.domain

import nl.nlportal.core.util.ObjectValidator
import nl.nlportal.data.domain.AbstractId
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.GeneratedValue
import org.hibernate.annotations.UuidGenerator

@Embeddable
data class TaskId(

    @Column(name = "task_id", columnDefinition = "UUID")
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue
    val value: UUID,

) : AbstractId<TaskId>() {

    init {
        ObjectValidator.validate(this)
    }

    companion object {

        fun existingId(value: UUID): TaskId {
            return TaskId(value)
        }

        fun newId(value: UUID): TaskId {
            val taskId = TaskId(value)
            taskId.newIdentity()
            return taskId
        }
    }
}