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
package nl.nlportal.case.graphql

import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.case.domain.Case
import java.time.format.DateTimeFormatter
import java.util.UUID

data class CaseInstance(
    val id: UUID,
    val externalId: String?,
    val userId: String,
    val status: Status?,
    val submission: ObjectNode,
    val statusHistory: List<HistoricStatus>?,
    val caseDefinitionId: String,
    val createdOn: String,
) {
    companion object {
        fun from(case: Case): CaseInstance {
            return CaseInstance(
                case.caseId.value,
                case.externalId,
                case.userId,
                Status(
                    case.status.name,
                    case.status.createdOn.format(DateTimeFormatter.ISO_DATE_TIME),
                ),
                case.submission.value,
                case.statusHistory?.map {
                    HistoricStatus(
                        Status(it.status.name, it.status.createdOn.format(DateTimeFormatter.ISO_DATE_TIME)),
                        it.createdOn.format(DateTimeFormatter.ISO_DATE_TIME),
                    )
                }?.toList(),
                case.caseDefinitionId.value,
                case.createdOn.format(DateTimeFormatter.ISO_DATE_TIME),
            )
        }
    }
}