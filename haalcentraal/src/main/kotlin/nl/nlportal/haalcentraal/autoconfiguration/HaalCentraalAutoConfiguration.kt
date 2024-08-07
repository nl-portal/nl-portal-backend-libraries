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
package nl.nlportal.haalcentraal.autoconfiguration

import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.core.ssl.ResourceClientSslContextResolver
import nl.nlportal.haalcentraal.client.HaalCentraalClientConfig
import nl.nlportal.haalcentraal.client.HaalCentraalClientProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader

@AutoConfiguration
@EnableConfigurationProperties(HaalCentraalClientConfig::class)
class HaalCentraalAutoConfiguration {
    @Bean
    @ConditionalOnProperty("nl-portal.haalcentraal.ssl.enabled", matchIfMissing = false)
    @ConditionalOnMissingBean(ClientSslContextResolver::class)
    fun clientSslContextResolver(resourceLoader: ResourceLoader): ClientSslContextResolver {
        return ResourceClientSslContextResolver(resourceLoader)
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraalClientProvider::class)
    fun haalCentraalClientProvider(
        haalCentraalClientConfig: HaalCentraalClientConfig,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
    ): HaalCentraalClientProvider {
        return HaalCentraalClientProvider(haalCentraalClientConfig, clientSslContextResolver)
    }
}