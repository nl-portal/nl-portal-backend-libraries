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
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(ProductConfig::class, DmnConfig::class, PrefillConfig::class)
class ProductAutoConfiguration {
    @Bean("dmnClient")
    fun dmnClient(
        dmnConfig: DmnConfig,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
        webClientBuilder: WebClient.Builder,
    ): DmnClient {
        return DmnClient(
            dmnConfig,
            clientSslContextResolver,
            webClientBuilder,
        )
    }

    @Bean
    fun productService(
        productConfig: ProductConfig,
        objectsApiClient: ObjectsApiClient,
        zakenApiClient: ZakenApiClient,
        taakObjectConfig: TaakObjectConfig,
        objectsApiTaskConfig: TaakObjectConfig,
        dmnClient: DmnClient,
    ): ProductService {
        return ProductService(
            productConfig,
            objectsApiClient,
            zakenApiClient,
            taakObjectConfig,
            objectsApiTaskConfig,
            dmnClient,
        )
    }

    @Bean("dmnService")
    fun dmnService(
        objectsApiClient: ObjectsApiClient,
        dmnClient: DmnClient,
        productService: ProductService,
    ): DmnService {
        return DmnService(
            objectsApiClient,
            dmnClient,
            productService,
        )
    }

    @Bean("prefillService")
    fun prefillService(
        prefillConfig: PrefillConfig,
        objectsApiClient: ObjectsApiClient,
        productService: ProductService,
    ): PrefillService {
        return PrefillService(
            prefillConfig,
            objectsApiClient,
            productService,
        )
    }

    @Bean
    fun productQuery(
        productService: ProductService,
        dmnService: DmnService,
        prefillService: PrefillService,
    ): ProductQuery {
        return ProductQuery(productService, dmnService, prefillService)
    }

    @Bean
    fun productMutation(productService: ProductService): ProductMutation {
        return ProductMutation(productService)
    }
}