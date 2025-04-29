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

import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.haalcentraal2.client.HaalCentraal2Client
import nl.nlportal.haalcentraal2.service.HaalCentraal2Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(HaalCentraal2ModuleConfiguration::class)
@ConditionalOnProperty(prefix = "nl-portal.config.haalcentraal2", name = ["enabled"], havingValue = "true")
class HaalCentraal2AutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(HaalCentraal2Client::class)
    fun haalCentraal2Client(
        haalCentraal2ModuleConfiguration: HaalCentraal2ModuleConfiguration,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
        webClientBuilder: WebClient.Builder,
    ): HaalCentraal2Client {
        return HaalCentraal2Client(
            haalCentraal2ConfigurationProperties = haalCentraal2ModuleConfiguration.properties,
            clientSslContextResolver = clientSslContextResolver,
            webClientBuilder = webClientBuilder,
        )
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraal2Service::class)
    fun haalCentraal2Service(
        haalCentraal2ModuleConfiguration: HaalCentraal2ModuleConfiguration,
        haalCentraal2BrpClient: HaalCentraal2Client,
    ): HaalCentraal2Service {
        return HaalCentraal2Service(
            haalCentraal2ConfigurationProperties = haalCentraal2ModuleConfiguration.properties,
            haalCentraal2Client = haalCentraal2BrpClient,
        )
    }
}