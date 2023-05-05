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
package com.ritense.portal.product.autoconfiguration

import com.ritense.portal.product.client.OpenFormulierenClient
import com.ritense.portal.product.client.OpenFormulierenClientConfig
import com.ritense.portal.product.graphql.FormQuery
import com.ritense.portal.product.service.FormService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenFormulierenClientConfig::class)
class ProductAutoConfiguration {

    @Bean
    fun openFormulierenClientConfig(): OpenFormulierenClientConfig {
        return OpenFormulierenClientConfig()
    }

    @Bean
    fun openFormulierenClient(
        openFormulierenClientConfig: OpenFormulierenClientConfig
    ): OpenFormulierenClient {
        return OpenFormulierenClient(openFormulierenClientConfig)
    }

    @Bean
    @ConditionalOnMissingBean(FormService::class)
    fun formService(
        openFormulierenClient: OpenFormulierenClient
    ): FormService {
        return com.ritense.portal.product.service.impl.FormService(openFormulierenClient)
    }

    @Bean
    fun formQuery(
        formService: FormService
    ): FormQuery {
        return FormQuery(formService)
    }
}