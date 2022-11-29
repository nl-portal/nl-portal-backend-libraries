/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class CaseId(

    @Column(name = "case_id", columnDefinition = "UUID")
    @org.hibernate.annotations.Type(type = "pg-uuid")
    val value: UUID

) : AbstractId<CaseId>() {

    init {
        ObjectValidator.validate(this)
    }

    companion object {

        fun existingId(value: UUID): CaseId {
            return CaseId(value)
        }

        fun newId(value: UUID): CaseId {
            val caseId = CaseId(value)
            caseId.newIdentity()
            return caseId
        }
    }
}