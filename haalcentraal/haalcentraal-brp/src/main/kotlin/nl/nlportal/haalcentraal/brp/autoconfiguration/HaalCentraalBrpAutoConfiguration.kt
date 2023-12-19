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
package nl.nlportal.haalcentraal.brp.autoconfiguration

import nl.nlportal.haalcentraal.brp.client.HaalCentraalBrpClient
import nl.nlportal.haalcentraal.brp.graphql.HaalCentraalBrpQuery
import nl.nlportal.haalcentraal.brp.service.HaalCentraalBrpService
import nl.nlportal.haalcentraal.brp.service.impl.HaalCentraalBrpServiceImpl as HaalCentraalBrpServiceImpl
import nl.nlportal.haalcentraal.client.HaalCentraalClientConfig
import nl.nlportal.haalcentraal.client.HaalCentraalClientProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@AutoConfiguration
@EnableConfigurationProperties(HaalCentraalClientConfig::class)
@Import(HaalCentraalClientProvider::class)
class HaalCentraalBrpAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpClient::class)
    fun haalCentraalBrpClient(haalCentraalClientProvider: HaalCentraalClientProvider): HaalCentraalBrpClient {
        return HaalCentraalBrpClient(haalCentraalClientProvider)
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpService::class)
    fun haalCentraalBrpServiceImpl(haalCentraalBrpClient: HaalCentraalBrpClient): HaalCentraalBrpService {
        return HaalCentraalBrpServiceImpl(haalCentraalBrpClient)
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpQuery::class)
    fun haalCentraalBrpQuery(haalCentraalBrpService: HaalCentraalBrpService): HaalCentraalBrpQuery {
        return HaalCentraalBrpQuery(haalCentraalBrpService)
    }
}