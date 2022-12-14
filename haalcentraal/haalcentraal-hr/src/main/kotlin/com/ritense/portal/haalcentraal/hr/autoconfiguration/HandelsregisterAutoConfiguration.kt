/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.haalcentraal.hr.autoconfiguration

import com.ritense.portal.haalcentraal.client.HaalCentraalClientProvider
import com.ritense.portal.haalcentraal.hr.client.HandelsregisterClient
import com.ritense.portal.haalcentraal.hr.graphql.HandelsregisterQuery
import com.ritense.portal.haalcentraal.hr.service.HandelsregisterService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HandelsregisterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(HandelsregisterClient::class)
    fun handelsregisterClient(
        haalCentraalClientProvider: HaalCentraalClientProvider
    ): HandelsregisterClient {
        return HandelsregisterClient(haalCentraalClientProvider)
    }

    @Bean
    @ConditionalOnMissingBean(HandelsregisterService::class)
    fun handelsregisterService(
        handelsregisterClient: HandelsregisterClient
    ): HandelsregisterService {
        return HandelsregisterService(handelsregisterClient)
    }

    @Bean
    @ConditionalOnMissingBean(HandelsregisterQuery::class)
    fun handelsregisterQuery(
        handelsregisterService: HandelsregisterService
    ): HandelsregisterQuery {
        return HandelsregisterQuery(handelsregisterService)
    }
}