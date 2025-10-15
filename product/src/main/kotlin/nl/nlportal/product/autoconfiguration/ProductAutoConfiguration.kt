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
package nl.nlportal.product.autoconfiguration

import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.product.client.DmnClient
import nl.nlportal.product.client.DmnConfig
import nl.nlportal.product.client.PrefillConfig
import nl.nlportal.product.graphql.ProductQuery
import nl.nlportal.product.client.ProductConfig
import nl.nlportal.product.graphql.ProductMutation
import nl.nlportal.product.service.DmnService
import nl.nlportal.product.service.PrefillService
import nl.nlportal.product.service.ProductService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.taak.autoconfigure.TaakConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(ProductConfig::class, DmnConfig::class, PrefillConfig::class)
class ProductAutoConfiguration {
    @Bean("dmnClient")
    @ConditionalOnProperty(prefix = "nl-portal.config.dmn", name = ["enabled"], havingValue = "true")
    fun dmnClient(
        dmnConfig: DmnConfig,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
        webClientBuilder: WebClient.Builder,
    ): DmnClient =
        DmnClient(
            dmnConfig.properties,
            clientSslContextResolver,
            webClientBuilder,
        )

    @Bean
    @ConditionalOnProperty(prefix = "nl-portal.config", name = ["product.enabled", "zakenapi.enabled"], havingValue = "true")
    fun productService(
        productConfig: ProductConfig,
        objectsApiClient: ObjectsApiClient,
        zakenApiClient: ZakenApiClient,
        taakObjectConfig: TaakConfig,
        authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService,
    ): ProductService =
        ProductService(
            productConfig.properties,
            taakObjectConfig.properties,
            objectsApiClient,
            zakenApiClient,
            authenticationMachtigingsDienstService,
        )

    @Bean("dmnService")
    @ConditionalOnProperty(prefix = "nl-portal.config.dmn", name = ["enabled"], havingValue = "true")
    @ConditionalOnBean(ProductService::class)
    fun dmnService(
        objectsApiClient: ObjectsApiClient,
        dmnClient: DmnClient,
        productService: ProductService,
    ): DmnService =
        DmnService(
            objectsApiClient,
            dmnClient,
            productService,
        )

    @Bean("prefillService")
    @ConditionalOnProperty(prefix = "nl-portal.config.prefill", name = ["enabled"], havingValue = "true")
    @ConditionalOnBean(ProductService::class)
    fun prefillService(
        prefillConfig: PrefillConfig,
        objectsApiClient: ObjectsApiClient,
        productService: ProductService,
    ): PrefillService =
        PrefillService(
            prefillConfig.properties,
            objectsApiClient,
            productService,
        )

    @Bean
    @ConditionalOnProperty(prefix = "nl-portal.config.product", name = ["enabled"], havingValue = "true")
    @ConditionalOnBean(ProductService::class)
    fun productQuery(
        productService: ProductService,
        dmnService: DmnService,
        prefillService: PrefillService,
    ): ProductQuery = ProductQuery(productService, dmnService, prefillService)

    @Bean
    @ConditionalOnProperty(prefix = "nl-portal.config.product", name = ["enabled"], havingValue = "true")
    @ConditionalOnBean(ProductService::class)
    fun productMutation(productService: ProductService): ProductMutation = ProductMutation(productService)
}