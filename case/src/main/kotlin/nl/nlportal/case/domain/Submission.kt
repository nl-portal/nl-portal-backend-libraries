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

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Embeddable
data class Submission(

    @Column(name = "submission", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    val value: ObjectNode,

) {

    fun update(properties: Map<JsonPointer, JsonNode>): Submission {
        val submission = this.value
        properties.entries.stream().forEach {
            val containerNode = submission.at(it.key.head()) as ObjectNode
            containerNode.replace(it.key.last().toString().removePrefix("/"), it.value)
        }
        return Submission(submission)
    }
}