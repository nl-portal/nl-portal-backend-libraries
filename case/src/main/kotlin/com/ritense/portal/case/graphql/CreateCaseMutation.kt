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
package com.ritense.portal.case.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.service.CaseService
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import graphql.schema.DataFetchingEnvironment

class CreateCaseMutation(private val caseService: CaseService) : Mutation {

    @GraphQLDescription("Convert submission to json return resulting data")
    fun processSubmission(
        submission: ObjectNode,
        caseDefinitionId: String,
        initialStatus: String? = null,
        dfe: DataFetchingEnvironment
    ): CaseCreated {
        val case = caseService.create(
            caseDefinitionId,
            submission,
            dfe.graphQlContext.get(AUTHENTICATION_KEY),
            initialStatus
        )
        return CaseCreated(case.caseId.value)
    }
}