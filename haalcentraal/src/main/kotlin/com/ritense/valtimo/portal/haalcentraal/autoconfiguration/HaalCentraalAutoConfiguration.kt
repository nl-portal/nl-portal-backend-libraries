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
package com.ritense.portal.haalcentraal.autoconfiguration

import com.ritense.portal.core.ssl.ClientSslContextResolver
import com.ritense.portal.core.ssl.ResourceClientSslContextResolver
import com.ritense.portal.haalcentraal.client.HaalCentraalClientConfig
import com.ritense.portal.haalcentraal.client.HaalCentraalClientProvider
import com.ritense.portal.haalcentraal.client.tokenexchange.KeyCloakUserTokenExchangeFilter
import com.ritense.portal.haalcentraal.client.tokenexchange.UserTokenExchangeFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(HaalCentraalClientConfig::class)
class HaalCentraalAutoConfiguration {

    @Bean
    @ConditionalOnProperty("valtimo.haalcentraal.ssl.enabled", matchIfMissing = false)
    @ConditionalOnMissingBean(ClientSslContextResolver::class)
    fun clientSslContextResolver(resourceLoader: ResourceLoader): ClientSslContextResolver {
        return ResourceClientSslContextResolver(resourceLoader)
    }

    @Bean
    @ConditionalOnProperty("valtimo.haalcentraal.tokenExchange.targetAudience", matchIfMissing = false)
    @ConditionalOnMissingBean(UserTokenExchangeFilter::class)
    fun userTokenExchangeFilterFactory(
        haalCentraalClientConfig: HaalCentraalClientConfig
    ): UserTokenExchangeFilter {
        requireNotNull(haalCentraalClientConfig.tokenExchange)
        return KeyCloakUserTokenExchangeFilter(WebClient.create(), haalCentraalClientConfig.tokenExchange.targetAudience)
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraalClientProvider::class)
    fun haalCentraalClientProvider(
        haalCentraalClientConfig: HaalCentraalClientConfig,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
        @Autowired(required = false) userTokenExchangeFilter: UserTokenExchangeFilter? = null
    ): HaalCentraalClientProvider {
        return HaalCentraalClientProvider(haalCentraalClientConfig, clientSslContextResolver, userTokenExchangeFilter)
    }
}