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

import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.core.util.ObjectValidator
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

@Entity
@Table(name = "task")
data class Task(

    @EmbeddedId
    val taskId: TaskId,

    @Column(name = "external_task_id", columnDefinition = "VARCHAR(255)")
    @field:Length(max = 255)
    @field:NotBlank
    val externalTaskId: String,

    @Column(name = "task_definition_key", columnDefinition = "VARCHAR(255)")
    @field:Length(max = 255)
    @field:NotBlank
    val taskDefinitionKey: String,

    @Column(name = "external_case_id", columnDefinition = "VARCHAR(1024)")
    @field:Length(max = 1024)
    @field:NotBlank
    val externalCaseId: String,

    @Column(name = "user_id", columnDefinition = "VARCHAR(1024)", nullable = false)
    @field:Length(max = 1024)
    @field:NotBlank
    val userId: String,

    @Column(name = "form_definition", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val formDefinition: ObjectNode,

    @Column(name = "completed", columnDefinition = "BOOLEAN")
    private var isCompleted: Boolean = false,

    @Column(name = "created_on", columnDefinition = "TIMESTAMPTZ", nullable = false)
    val createdOn: LocalDateTime = LocalDateTime.now(),

    @Column(name = "public", columnDefinition = "BOOLEAN")
    val isPublic: Boolean = false,
) : Persistable<TaskId> {

    init {
        ObjectValidator.validate(this)
    }

    override fun getId(): TaskId {
        return taskId
    }

    override fun isNew(): Boolean {
        return taskId.isNew()
    }

    fun completeTask() {
        isCompleted = true
    }

    fun isCompleted(): Boolean {
        return isCompleted
    }
}