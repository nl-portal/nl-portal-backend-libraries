/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.autoconfigure

import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.openproduct.client.OpenProductClient
import nl.nlportal.openproduct.client.OpenProductDmnClient
import nl.nlportal.openproduct.client.OpenProductTypeClient
import nl.nlportal.openproduct.service.OpenProductDmnService
import nl.nlportal.openproduct.service.OpenProductService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.taak.autoconfigure.TaakConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(
    OpenProductModuleConfiguration::class,
    OpenProductDmnConfiguration::class,
)
@ConditionalOnProperty(prefix = "nl-portal.config.openproduct", name = ["enabled"], havingValue = "true")
class OpenProductAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(OpenProductClient::class)
    fun openProductClient(openProductModuleConfiguration: OpenProductModuleConfiguration): OpenProductClient =
        OpenProductClient(
            openProductConfigurationProperties = openProductModuleConfiguration.properties,
        )

    @Bean
    @ConditionalOnMissingBean(OpenProductTypeClient::class)
    fun openProductTypeClient(openProductModuleConfiguration: OpenProductModuleConfiguration): OpenProductTypeClient =
        OpenProductTypeClient(
            openProductConfigurationProperties = openProductModuleConfiguration.properties,
        )

    @Bean
    @ConditionalOnMissingBean(OpenProductService::class)
    fun openProductService(
        openProductClient: OpenProductClient,
        openProductTypeClient: OpenProductTypeClient,
        zakenApiClient: ZakenApiClient,
        objectsApiClient: ObjectsApiClient,
        taakObjectConfig: TaakConfig,
        authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService,
    ): OpenProductService =
        OpenProductService(
            openProductClient = openProductClient,
            openProductTypeClient = openProductTypeClient,
            objectsApiTaskConfigProperties = taakObjectConfig.properties,
            zakenApiClient = zakenApiClient,
            objectsApiClient = objectsApiClient,
            authenticationMachtigingsDienstService = authenticationMachtigingsDienstService,
        )

    @Bean("openProductDmnClient")
    @ConditionalOnProperty(prefix = "nl-portal.config.openproduct.dmn", name = ["enabled"], havingValue = "true")
    fun openProductDmnClient(
        dmnConfiguration: OpenProductDmnConfiguration,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
        webClientBuilder: WebClient.Builder,
    ): OpenProductDmnClient =
        OpenProductDmnClient(
            dmnConfiguration.properties,
            clientSslContextResolver,
            webClientBuilder,
        )

    @Bean
    fun openProductDmnService(
        openProductDmnClient: OpenProductDmnClient,
        openProductService: OpenProductService,
    ): OpenProductDmnService =
        OpenProductDmnService(
            openProductDmnClient = openProductDmnClient,
            openProductService = openProductService,
        )
}