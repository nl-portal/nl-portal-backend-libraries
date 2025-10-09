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

import java.util.UUID
import nl.nlportal.case.domain.CaseId
import nl.nlportal.case.service.CaseService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CaseInstanceQuery(
    private val caseService: CaseService,
) {
    @QueryMapping
    fun allCaseInstances(
        @Argument orderBy: CaseInstanceOrdering = CaseInstanceOrdering(createdOn = Sort.DESC),
        authentication: CommonGroundAuthentication,
    ): List<CaseInstance> {
        var caseInstances = caseService.getAllCases(authentication.userId)
        if (orderBy.createdOn == Sort.ASC) {
            caseInstances = caseInstances.sortedBy { it.createdOn }
        } else if (orderBy.createdOn == Sort.DESC) {
            caseInstances = caseInstances.sortedByDescending { it.createdOn }
        }
        return caseInstances.map { CaseInstance.from(it) }
    }

    @QueryMapping
    fun getCaseInstance(
        @Argument id: UUID,
        authentication: CommonGroundAuthentication,
    ): CaseInstance? {
        val case =
            caseService.getCase(
                CaseId.existingId(id),
                authentication.userId,
            ) ?: return null
        return CaseInstance.from(case)
    }
}