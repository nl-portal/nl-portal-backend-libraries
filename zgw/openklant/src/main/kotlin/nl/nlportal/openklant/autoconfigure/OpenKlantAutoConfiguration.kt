/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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

import mu.KotlinLogging
import nl.nlportal.openklant.client.OpenKlant2Client
import nl.nlportal.openklant.service.OpenKlant2Service
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    OpenKlantModuleConfiguration::class,
)
class OpenKlantAutoConfiguration {
    @Bean
    fun openKlant2Client(openklantModuleConfiguration: OpenKlantModuleConfiguration): OpenKlant2Client {
        if (!openklantModuleConfiguration.enabled) {
            logger.debug { "OpenKlant 2 is not configured." }
        }
        return OpenKlant2Client(openKlantConfigurationProperties = openklantModuleConfiguration.properties)
    }

    @Bean
    fun openKlant2Service(
        openklantModuleConfiguration: OpenKlantModuleConfiguration,
        openklant2Client: OpenKlant2Client
    ): OpenKlant2Service {
        return OpenKlant2Service(openklantModuleConfiguration.enabled, openklant2Client)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}