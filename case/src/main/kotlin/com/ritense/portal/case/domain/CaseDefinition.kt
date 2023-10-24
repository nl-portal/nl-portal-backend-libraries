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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.core.util.ObjectValidator
import com.ritense.portal.data.domain.AggregateRoot
import com.ritense.portal.data.domain.DomainEvent
import org.hibernate.validator.constraints.Length
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "case_definition")
data class CaseDefinition(

    @EmbeddedId
    val caseDefinitionId: CaseDefinitionId,

    @Column(name = "external_id", columnDefinition = "VARCHAR(1024)")
    @field:Length(max = 1024)
    var externalId: String? = null,

    @Embedded
    var schema: Schema,

    @Embedded
    val statusDefinition: StatusDefinition,

    @Column(name = "created_on", columnDefinition = "TIMESTAMPTZ", nullable = false)
    val createdOn: LocalDateTime = LocalDateTime.now(),

) : Persistable<CaseDefinitionId>, AggregateRoot<DomainEvent>() {

    init {
        ObjectValidator.validate(this)
    }

    override fun getId(): CaseDefinitionId {
        return caseDefinitionId
    }

    override fun isNew(): Boolean {
        return caseDefinitionId.isNew()
    }

    fun modify(schema: ObjectNode) {
        this.schema = Schema(schema)
    }
}