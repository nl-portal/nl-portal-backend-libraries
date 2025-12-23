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

import tools.jackson.databind.node.ObjectNode
import nl.nlportal.case.service.CaseService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class CreateCaseMutation(
    private val caseService: CaseService,
) {
    @MutationMapping
    fun processSubmission(
        @Argument submission: ObjectNode,
        @Argument caseDefinitionId: String,
        @Argument initialStatus: String? = null,
        authentication: CommonGroundAuthentication,
    ): CaseCreated {
        val case =
            caseService.create(
                caseDefinitionId,
                submission,
                authentication,
                initialStatus,
            )
        return CaseCreated(case.caseId.value)
    }
}