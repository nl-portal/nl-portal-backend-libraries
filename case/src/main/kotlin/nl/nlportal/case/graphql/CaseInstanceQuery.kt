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
import nl.nlportal.case.domain.CaseId
import nl.nlportal.case.service.CaseService
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import graphql.schema.DataFetchingEnvironment
import org.springframework.security.core.Authentication
import java.util.UUID

class CaseInstanceQuery(private val caseService: CaseService) : Query {

    @GraphQLDescription("retrieves all available case instances")
    fun allCaseInstances(
        @GraphQLDescription("The case instance orderBy ")
        orderBy: CaseInstanceOrdering = CaseInstanceOrdering(createdOn = Sort.DESC),
        dfe: DataFetchingEnvironment,
    ): List<CaseInstance> {
        var caseInstances = caseService.getAllCases(dfe.graphQlContext.get<Authentication>(AUTHENTICATION_KEY).name)
        if (orderBy.createdOn == Sort.ASC) {
            caseInstances = caseInstances.sortedBy { it.createdOn }
        } else if (orderBy.createdOn == Sort.DESC) {
            caseInstances = caseInstances.sortedByDescending { it.createdOn }
        }
        return caseInstances.map { CaseInstance.from(it) }
    }

    @GraphQLDescription("retrieves single case instance from repository")
    fun getCaseInstance(
        @GraphQLDescription("The case instance id ") id: UUID,
        dfe: DataFetchingEnvironment,
    ): CaseInstance? {
        val case = caseService.getCase(
            CaseId.existingId(id),
            dfe.graphQlContext.get<Authentication>(AUTHENTICATION_KEY).name,
        ) ?: return null
        return CaseInstance.from(case)
    }
}