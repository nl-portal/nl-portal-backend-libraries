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

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.case.service.CaseDefinitionService

class CaseDefinitionQuery(val caseDefinitionService: CaseDefinitionService) : Query {
    @GraphQLDescription("retrieves all available case definitions")
    fun allCaseDefinitions(): List<CaseDefinition> {
        return caseDefinitionService.getAllCaseDefinitions().map {
            CaseDefinition(
                it.caseDefinitionId.value,
                it.schema.value,
                it.statusDefinition.statuses.toList(),
            )
        }
    }
}