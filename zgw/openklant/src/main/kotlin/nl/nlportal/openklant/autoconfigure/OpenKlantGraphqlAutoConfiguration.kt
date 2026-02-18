/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.autoconfigure

import nl.nlportal.openklant.graphql.DigitaleAdresMutation
import nl.nlportal.openklant.graphql.DigitaleAdresQuery
import nl.nlportal.openklant.graphql.KlantContactQuery
import nl.nlportal.openklant.graphql.PartijMutation
import nl.nlportal.openklant.graphql.PartijQuery
import nl.nlportal.openklant.service.OpenKlant2Service
import nl.nlportal.verificatie.autoconfigure.VerificatieModuleConfiguration
import nl.nlportal.verificatie.service.VerificatieService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

@ConditionalOnProperty(prefix = "nl-portal.config", name = ["openklant2.enabled"], havingValue = "true")
class OpenKlantGraphqlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(PartijQuery::class)
    fun partijQuery(openKlant2Service: OpenKlant2Service) = PartijQuery(openKlant2Service)

    @Bean
    @ConditionalOnMissingBean(PartijMutation::class)
    fun partijMutation(openKlant2Service: OpenKlant2Service) = PartijMutation(openKlant2Service)

    @Bean
    @ConditionalOnMissingBean(DigitaleAdresQuery::class)
    fun digitaleAdresQuery(openKlant2Service: OpenKlant2Service) = DigitaleAdresQuery(openKlant2Service)

    @Bean
    @ConditionalOnMissingBean(DigitaleAdresMutation::class)
    fun digitaleAdresMutation(
        openKlant2Service: OpenKlant2Service,
        verificatieModuleConfiguration: VerificatieModuleConfiguration,
        verificatieService: VerificatieService?,
    ) = DigitaleAdresMutation(
        openklant2Service = openKlant2Service,
        verificatieModuleConfiguration = verificatieModuleConfiguration,
        verificatieService = verificatieService,
    )

    @Bean
    @ConditionalOnMissingBean(KlantContactQuery::class)
    fun klantContactQuery(openKlant2Service: OpenKlant2Service) = KlantContactQuery(openKlant2Service)
}