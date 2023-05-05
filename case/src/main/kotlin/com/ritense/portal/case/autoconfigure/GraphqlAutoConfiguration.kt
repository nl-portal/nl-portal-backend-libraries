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
package com.ritense.portal.case.autoconfigure

import com.ritense.portal.case.graphql.CaseDefinitionQuery
import com.ritense.portal.case.graphql.CaseInstanceQuery
import com.ritense.portal.case.graphql.CreateCaseMutation
import com.ritense.portal.case.service.CaseDefinitionService
import com.ritense.portal.case.service.CaseService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphqlAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CreateCaseMutation::class)
    fun createCaseMutation(caseService: CaseService): CreateCaseMutation {
        return CreateCaseMutation(caseService)
    }

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionQuery::class)
    fun caseDefinitionQuery(caseDefinitionService: CaseDefinitionService): CaseDefinitionQuery {
        return CaseDefinitionQuery(caseDefinitionService)
    }

    @Bean
    @ConditionalOnMissingBean(CaseInstanceQuery::class)
    fun caseInstanceQuery(caseService: CaseService): CaseInstanceQuery {
        return CaseInstanceQuery(caseService)
    }
}