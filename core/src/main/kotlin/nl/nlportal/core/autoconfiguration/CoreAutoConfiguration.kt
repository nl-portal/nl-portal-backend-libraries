/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package nl.nlportal.core.autoconfiguration

import nl.nlportal.core.frontend.configuration.FrontendFeaturesConfigurationProperties
import nl.nlportal.core.frontend.configuration.FrontendThemeConfigurationProperties
import nl.nlportal.core.frontend.service.FrontendFeaturesConfigurationService
import nl.nlportal.core.frontend.service.FrontendThemeConfigurationService
import nl.nlportal.core.frontend.web.rest.FrontendFeaturesConfigurationResource
import nl.nlportal.core.frontend.web.rest.FrontendThemeConfigurationResource
import nl.nlportal.core.util.Mapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import tools.jackson.databind.json.JsonMapper

@AutoConfiguration
@EnableConfigurationProperties(
    FrontendThemeConfigurationProperties::class,
    FrontendFeaturesConfigurationProperties::class,
)
class CoreAutoConfiguration {
    @Bean("objectMapper")
    @ConditionalOnMissingBean(name = ["jsonMapper"])
    fun objectMapper(): JsonMapper = Mapper.get()

    @Bean
    @ConditionalOnMissingBean(FrontendThemeConfigurationService::class)
    fun frontendThemeConfigurationService(
        coreThemeConfigurationProperties: FrontendThemeConfigurationProperties,
    ): FrontendThemeConfigurationService = FrontendThemeConfigurationService(coreThemeConfigurationProperties)

    @Bean
    @ConditionalOnMissingBean(FrontendThemeConfigurationResource::class)
    fun frontendThemeConfigurationResource(
        frontendConfigurationService: FrontendThemeConfigurationService,
    ): FrontendThemeConfigurationResource = FrontendThemeConfigurationResource(frontendConfigurationService)

    @Bean
    @ConditionalOnMissingBean(FrontendFeaturesConfigurationService::class)
    fun frontendFeaturesConfigurationService(
        frontendFeaturesConfigurationProperties: FrontendFeaturesConfigurationProperties,
    ): FrontendFeaturesConfigurationService = FrontendFeaturesConfigurationService(frontendFeaturesConfigurationProperties)

    @Bean
    @ConditionalOnMissingBean(FrontendFeaturesConfigurationResource::class)
    fun frontendFeaturesConfigurationResource(
        frontendFeaturesConfigurationService: FrontendFeaturesConfigurationService,
    ): FrontendFeaturesConfigurationResource = FrontendFeaturesConfigurationResource(frontendFeaturesConfigurationService)
}