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
package nl.nlportal.haalcentraal.hr.autoconfiguration

import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.core.ssl.ResourceClientSslContextResolver
import nl.nlportal.haalcentraal.hr.client.HaalCentraalHrClientConfig
import nl.nlportal.haalcentraal.hr.client.HandelsregisterClient
import nl.nlportal.haalcentraal.hr.graphql.HandelsregisterQuery
import nl.nlportal.haalcentraal.hr.service.HandelsregisterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader

@AutoConfiguration
@EnableConfigurationProperties(HaalCentraalHrClientConfig::class)
class HandelsregisterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ClientSslContextResolver::class)
    fun clientSslContextResolver(resourceLoader: ResourceLoader): ClientSslContextResolver {
        return ResourceClientSslContextResolver(resourceLoader)
    }

    @Bean
    @ConditionalOnMissingBean(HandelsregisterClient::class)
    fun handelsregisterClient(
        haalCentraalHrClientConfig: HaalCentraalHrClientConfig,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
    ): HandelsregisterClient {
        return HandelsregisterClient(
            haalCentraalHrClientConfig,
            clientSslContextResolver,
        )
    }

    @Bean
    @ConditionalOnMissingBean(HandelsregisterService::class)
    fun handelsregisterService(handelsregisterClient: HandelsregisterClient): HandelsregisterService {
        return HandelsregisterService(handelsregisterClient)
    }

    @Bean
    @ConditionalOnMissingBean(HandelsregisterQuery::class)
    fun handelsregisterQuery(handelsregisterService: HandelsregisterService): HandelsregisterQuery {
        return HandelsregisterQuery(handelsregisterService)
    }
}