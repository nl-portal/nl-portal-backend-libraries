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
package nl.nlportal.case.domain

import nl.nlportal.core.util.ObjectValidator
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Embeddable
data class StatusDefinition(

    @Column(name = "status_definition", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val statuses: List<String>,

) {

    init {
        ObjectValidator.validate(this)
    }

    fun validateStatus(status: String) {
        if (!statuses.contains(status)) {
            throw IllegalStateException("Invalid status $status, allowed: ${statuses.joinToString(",")}")
        }
    }
}