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
package nl.nlportal.startform.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "start_form")
class StartForm(
    @Id
    val id: UUID,
    @Column(name = "form_name")
    val formName: String,
    @Column(name = "type_uuid")
    val typeUUID: UUID,
    @Column(name = "type_version")
    val typeVersion: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StartForm

        if (id != other.id) return false
        if (formName != other.formName) return false
        if (typeUUID != other.typeUUID) return false
        if (typeVersion != other.typeVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + formName.hashCode()
        result = 31 * result + typeUUID.hashCode()
        result = 31 * result + typeVersion
        return result
    }
}