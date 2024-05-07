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

import nl.nlportal.backend.product.graphql.ProductQuery
import nl.nlportal.product.client.OpenFormulierenClient
import nl.nlportal.product.client.OpenFormulierenClientConfig
import nl.nlportal.product.client.ProductConfig
import nl.nlportal.product.graphql.FormQuery
import nl.nlportal.product.graphql.ProductMutation
import nl.nlportal.product.service.FormService
import nl.nlportal.product.service.ProductService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.ZakenApiConfig
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OpenFormulierenClientConfig::class, ProductConfig::class)
class ProductAutoConfiguration {
    @Bean
    fun openFormulierenClientConfig(): OpenFormulierenClientConfig {
        return OpenFormulierenClientConfig()
    }

    @Bean
    fun openFormulierenClient(openFormulierenClientConfig: OpenFormulierenClientConfig): OpenFormulierenClient {
        return OpenFormulierenClient(openFormulierenClientConfig)
    }

    @Bean
    @ConditionalOnMissingBean(FormService::class)
    fun formService(openFormulierenClient: OpenFormulierenClient): FormService {
        return nl.nlportal.product.service.impl.FormService(openFormulierenClient)
    }

    @Bean
    fun formQuery(formService: FormService): FormQuery {
        return FormQuery(formService)
    }

    @Bean
    fun productService(
        productConfig: ProductConfig,
        objectsApiClient: ObjectsApiClient,
        zakenApiClient: ZakenApiClient,
        taakObjectConfig: TaakObjectConfig,
        zakenApiConfig: ZakenApiConfig,
        objectsApiTaskConfig: TaakObjectConfig,
    ): ProductService {
        return ProductService(
            productConfig,
            objectsApiClient,
            zakenApiClient,
            taakObjectConfig,
            zakenApiConfig,
            objectsApiTaskConfig,
        )
    }

    @Bean
    fun productQuery(productService: ProductService): ProductQuery {
        return ProductQuery(productService)
    }

    @Bean
    fun productMutation(productService: ProductService): ProductMutation {
        return ProductMutation(productService)
    }
}