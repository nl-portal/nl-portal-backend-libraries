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

import com.ritense.portal.core.util.ObjectValidator
import com.ritense.portal.data.domain.AbstractId
import org.hibernate.validator.constraints.Length
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CaseDefinitionId(

    @Column(name = "case_definition_id", columnDefinition = "VARCHAR(255)")
    @field:Length(max = 255)
    val value: String,

) : AbstractId<CaseDefinitionId>() {

    init {
        ObjectValidator.validate(this)
    }

    companion object {

        fun existingId(id: String): CaseDefinitionId {
            return CaseDefinitionId(id)
        }

        fun newId(id: String): CaseDefinitionId {
            val caseDefinitionId = CaseDefinitionId(id)
            caseDefinitionId.newIdentity()
            return caseDefinitionId
        }
    }
}