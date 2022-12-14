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
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
data class Status(
    @Column(name = "status_name", columnDefinition = "VARCHAR(1024)", nullable = false)
    @field:Length(max = 1024)
    @field:NotBlank
    var name: String,

    @Column(name = "status_created_on", columnDefinition = "TIMESTAMPTZ", nullable = false)
    val createdOn: LocalDateTime = LocalDateTime.now()
) {

    init {
        ObjectValidator.validate(this)
    }
}