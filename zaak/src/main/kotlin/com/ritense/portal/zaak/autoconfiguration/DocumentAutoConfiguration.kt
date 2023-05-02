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
import com.ritense.portal.zaak.security.config.DocumentContentResourceHttpSecurityConfigurer
import com.ritense.portal.zaak.service.ZaakService
import com.ritense.portal.zaak.web.rest.DocumentContentResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenZaakClientConfig::class)
class DocumentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DocumentContentResourceHttpSecurityConfigurer::class)
    fun documentContentResourceHttpSecurityConfigurer(): DocumentContentResourceHttpSecurityConfigurer {
        return DocumentContentResourceHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(DocumentContentResource::class)
    fun documentContentResource(
        openZaakClient: OpenZaakClient,
        zaakService: ZaakService
    ): DocumentContentResource {
        return com.ritense.portal.zaak.web.rest.impl.DocumentContentResource(openZaakClient, zaakService)
    }
}