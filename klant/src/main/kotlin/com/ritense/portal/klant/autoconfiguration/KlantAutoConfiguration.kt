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
package com.ritense.portal.klant.autoconfiguration

import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import com.ritense.portal.klant.client.OpenKlantClient
import com.ritense.portal.klant.client.OpenKlantClientConfig
import com.ritense.portal.klant.graphql.BurgerMutation
import com.ritense.portal.klant.graphql.BurgerQuery
import com.ritense.portal.klant.service.BurgerService
import com.ritense.portal.klant.validation.GraphQlValidator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OpenKlantClientConfig::class)
class KlantAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BurgerService::class)
    fun burgerService(
        openKlantClientConfig: OpenKlantClientConfig,
        openKlantClient: OpenKlantClient
    ): BurgerService {
        return com.ritense.portal.klant.service.impl.BurgerService(openKlantClientConfig, openKlantClient)
    }

    @Bean
    fun openKlantClientConfig(): OpenKlantClientConfig {
        return OpenKlantClientConfig()
    }

    @Bean
    fun openKlantClient(
        openKlantClientConfig: OpenKlantClientConfig,
        idTokenGenerator: IdTokenGenerator
    ): OpenKlantClient {
        return OpenKlantClient(openKlantClientConfig, idTokenGenerator)
    }

    @Bean
    fun burgerQuery(burgerService: BurgerService): BurgerQuery {
        return BurgerQuery(burgerService)
    }

    @Bean
    fun burgerMutation(burgerService: BurgerService, graphQlValidator: GraphQlValidator): BurgerMutation {
        return BurgerMutation(burgerService, graphQlValidator)
    }

    @Bean
    fun graphqlValidator(): GraphQlValidator {
        return GraphQlValidator()
    }
}