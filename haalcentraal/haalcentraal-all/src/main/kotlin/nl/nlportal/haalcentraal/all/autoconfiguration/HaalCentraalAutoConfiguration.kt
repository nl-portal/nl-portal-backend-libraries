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
package nl.nlportal.haalcentraal.all.autoconfiguration

import nl.nlportal.haalcentraal.all.graphql.GemachtigdeQuery
import nl.nlportal.haalcentraal.brp.service.HaalCentraalBrpService
import nl.nlportal.haalcentraal.client.HaalCentraalClientConfig
import nl.nlportal.haalcentraal.hr.service.HandelsregisterService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(HaalCentraalClientConfig::class)
class HaalCentraalAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(GemachtigdeQuery::class)
    fun gemachtigdeQuery(
        haalCentraalBrpService: HaalCentraalBrpService,
        handelsregisterService: HandelsregisterService,
    ): GemachtigdeQuery {
        return GemachtigdeQuery(haalCentraalBrpService, handelsregisterService)
    }
}