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
package nl.nlportal.catalogiapi.autoconfigure

import nl.nlportal.catalogiapi.client.CatalogiApiClient
import nl.nlportal.catalogiapi.client.CatalogiApiConfig
import nl.nlportal.catalogiapi.service.CatalogiApiService
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(CatalogiApiConfig::class)
class CatalogiApiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CatalogiApiService::class)
    fun catalogiApiService(catalogiApiClient: CatalogiApiClient): CatalogiApiService {
        return CatalogiApiService(catalogiApiClient)
    }

    @Bean
    fun catalogiApiConfig(): CatalogiApiConfig {
        return CatalogiApiConfig()
    }

    @Bean
    fun catalogiApiClient(
        catalogiApiConfig: CatalogiApiConfig,
        idTokenGenerator: IdTokenGenerator,
    ): CatalogiApiClient {
        return CatalogiApiClient(catalogiApiConfig, idTokenGenerator)
    }
}