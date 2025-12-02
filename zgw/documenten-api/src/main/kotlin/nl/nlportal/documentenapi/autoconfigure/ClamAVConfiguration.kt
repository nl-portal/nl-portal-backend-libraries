/*
 * Copyright 2025 Ritense BV, the Netherlands.
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

package nl.nlportal.documentenapi.autoconfigure

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.documentenapi.client.ClamAVVirusScanConfig
import nl.nlportal.documentenapi.service.VirusScanService
import nl.nlportal.documentenapi.service.impl.ClamAVService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(ClamAVVirusScanConfig::class)
@ConditionalOnProperty(prefix = "nl-portal.config.virusscan.clamav", name = ["enabled"], havingValue = "true")
class ClamAVConfiguration {
    @Bean
    @ConditionalOnMissingBean(VirusScanService::class)
    fun virusScanService(
        clamAVVirusScanConfig: ClamAVVirusScanConfig
    ): VirusScanService {
        logger.info {
            "ClamAV virusscan is loaded with host: ${clamAVVirusScanConfig.properties.hostName} and port: ${clamAVVirusScanConfig.properties.port}"
        }
        return ClamAVService(clamAVVirusScanConfig.properties)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
