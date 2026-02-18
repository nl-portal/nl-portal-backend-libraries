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

import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.verificatie.client.VerificatieClient
import nl.nlportal.verificatie.service.VerificatieService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(VerificatieModuleConfiguration::class)
class VerificatieAutoConfiguration {
    @Bean("verificatieClient")
    @ConditionalOnMissingBean(VerificatieClient::class)
    @ConditionalOnProperty(prefix = "nl-portal.config", name = ["verificatie.enabled"], havingValue = "true")
    fun verificatieClient(
        verificatieModuleConfiguration: VerificatieModuleConfiguration,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
    ): VerificatieClient =
        VerificatieClient(
            verificatieConfigurationProperties = verificatieModuleConfiguration.properties,
            clientSslContextResolver = clientSslContextResolver,
        )

    @Bean
    @ConditionalOnMissingBean(VerificatieService::class)
    @ConditionalOnProperty(prefix = "nl-portal.config", name = ["verificatie.enabled"], havingValue = "true")
    fun verificatieService(
        verificatieModuleConfiguration: VerificatieModuleConfiguration,
        verificatieClient: VerificatieClient,
    ) = VerificatieService(
        verificatieConfigurationProperties = verificatieModuleConfiguration.properties,
        verificatieClient = verificatieClient,
    )
}