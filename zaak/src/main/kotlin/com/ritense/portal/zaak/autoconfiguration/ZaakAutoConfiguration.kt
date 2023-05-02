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
package com.ritense.portal.zaak.autoconfiguration

import com.ritense.portal.zaak.client.OpenZaakClient
import com.ritense.portal.zaak.client.OpenZaakClientConfig
import com.ritense.portal.zaak.client.OpenZaakTokenGenerator
import com.ritense.portal.zaak.graphql.DocumentContentQuery
import com.ritense.portal.zaak.graphql.ZaakQuery
import com.ritense.portal.zaak.service.ZaakService
import com.ritense.portal.zaak.service.impl.OpenZaakService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenZaakClientConfig::class)
class ZaakAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ZaakService::class)
    fun openZaakService(
        openZaakClient: OpenZaakClient,
        openZaakClientConfig: OpenZaakClientConfig
    ): ZaakService {
        return OpenZaakService(openZaakClient, openZaakClientConfig)
    }

    @Bean
    fun openZaakClientConfig(): OpenZaakClientConfig {
        return OpenZaakClientConfig()
    }

    @Bean
    fun openZaakTokenGenerator(): OpenZaakTokenGenerator {
        return OpenZaakTokenGenerator()
    }

    @Bean
    fun openZaakClient(
        openZaakClientConfig: OpenZaakClientConfig,
        openZaakTokenGenerator: OpenZaakTokenGenerator
    ): OpenZaakClient {
        return OpenZaakClient(openZaakClientConfig, openZaakTokenGenerator)
    }

    @Bean
    fun zaakListQuery(zaakService: ZaakService): ZaakQuery {
        return ZaakQuery(zaakService)
    }

    @Bean
    fun documentContentQuery(zaakService: ZaakService): DocumentContentQuery {
        return DocumentContentQuery(zaakService)
    }
}