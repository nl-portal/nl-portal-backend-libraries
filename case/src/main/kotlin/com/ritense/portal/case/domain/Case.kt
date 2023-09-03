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
package com.ritense.portal.case.domain

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.portal.core.util.ObjectValidator
import com.ritense.portal.data.domain.AggregateRoot
import com.ritense.portal.data.domain.DomainEvent
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Embedded
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

@Entity
@Table(name = "`case`")
data class Case(

    @EmbeddedId
    val caseId: CaseId,

    @Column(name = "external_id", columnDefinition = "VARCHAR(1024)")
    @field:Length(max = 1024)
    var externalId: String? = null,

    @Column(name = "user_id", columnDefinition = "VARCHAR(1024)", nullable = false)
    @field:Length(max = 1024)
    @field:NotBlank
    val userId: String,

    @Embedded
    var status: Status,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_history", columnDefinition = "json")
    var statusHistory: MutableList<HistoricStatus>? = null,

    @Embedded
    var submission: Submission,

    @Embedded
    val caseDefinitionId: CaseDefinitionId,

    @Column(name = "created_on", columnDefinition = "TIMESTAMPTZ", nullable = false)
    val createdOn: LocalDateTime = LocalDateTime.now()

) : Persistable<CaseId>, AggregateRoot<DomainEvent>() {

    init {
        ObjectValidator.validate(this)
    }

    fun changeStatus(status: Status): Status {
        this.statusHistory = this.statusHistory ?: ArrayList()
        this.statusHistory!!.add(HistoricStatus(this.status))
        this.status = status
        return this.status
    }

    fun updateSubmission(properties: Map<JsonPointer, JsonNode>): Submission {
        this.submission = this.submission.update(properties)
        return this.submission
    }

    override fun getId(): CaseId {
        return caseId
    }

    override fun isNew(): Boolean {
        return caseId.isNew()
    }
}