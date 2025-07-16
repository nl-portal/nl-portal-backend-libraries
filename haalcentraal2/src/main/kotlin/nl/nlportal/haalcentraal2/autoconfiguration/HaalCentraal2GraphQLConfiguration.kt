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
package nl.nlportal.haalcentraal2.autoconfiguration

import nl.nlportal.haalcentraal.hr.service.HandelsregisterService
import nl.nlportal.haalcentraal2.graphql.HaalCentraal2BewoningQuery
import nl.nlportal.haalcentraal2.graphql.HaalCentraal2BrpQuery
import nl.nlportal.haalcentraal2.graphql.HaalCentraal2GemachtigdeQuery
import nl.nlportal.haalcentraal2.service.HaalCentraal2Service
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

@ConditionalOnProperty(prefix = "nl-portal.config.haalcentraal2", name = ["enabled"], havingValue = "true")
class HaalCentraal2GraphQLConfiguration {
    @Bean
    @ConditionalOnMissingBean(HaalCentraal2BrpQuery::class)
    fun haalCentraal2BrpQuery(haalCentraal2Service: HaalCentraal2Service): HaalCentraal2BrpQuery =
        HaalCentraal2BrpQuery(
            haalCentraal2Service = haalCentraal2Service,
        )

    @Bean
    @ConditionalOnMissingBean(HaalCentraal2BewoningQuery::class)
    fun haalCentraal2BewoningQuery(haalCentraal2Service: HaalCentraal2Service): HaalCentraal2BewoningQuery =
        HaalCentraal2BewoningQuery(
            haalCentraal2Service = haalCentraal2Service,
        )

    @Bean
    @ConditionalOnMissingBean(HaalCentraal2GemachtigdeQuery::class)
    fun haalCentraal2GemachtigdeQuery(
        haalCentraal2Service: HaalCentraal2Service,
        handelsregisterService: HandelsregisterService,
    ): HaalCentraal2GemachtigdeQuery =
        HaalCentraal2GemachtigdeQuery(
            haalCentraal2Service = haalCentraal2Service,
            handelsregisterService = handelsregisterService,
        )
}