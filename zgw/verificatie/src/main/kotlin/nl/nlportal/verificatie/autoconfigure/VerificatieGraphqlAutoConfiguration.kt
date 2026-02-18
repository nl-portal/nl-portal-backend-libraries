/*
 * Copyright 2026 Ritense BV, the Netherlands.
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
package nl.nlportal.verificatie.autoconfigure

import nl.nlportal.verificatie.graphql.VerificatieMutation
import nl.nlportal.verificatie.graphql.VerificatieQuery
import nl.nlportal.verificatie.service.VerificatieService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

class VerificatieGraphqlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(VerificatieMutation::class)
    @ConditionalOnProperty(prefix = "nl-portal.config", name = ["verificatie.enabled"], havingValue = "true")
    fun verificatieMutation(verificatieService: VerificatieService) = VerificatieMutation(verificatieService)

    @Bean
    @ConditionalOnMissingBean(VerificatieQuery::class)
    fun verificatieQuery(verificatieModuleConfiguration: VerificatieModuleConfiguration) = VerificatieQuery(verificatieModuleConfiguration)
}