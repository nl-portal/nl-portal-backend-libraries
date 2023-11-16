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
package nl.nlportal.klant.autoconfiguration

import nl.nlportal.klant.client.OpenKlantClient
import nl.nlportal.klant.graphql.BurgerMutation
import nl.nlportal.klant.graphql.BurgerQuery
import nl.nlportal.klant.service.BurgerService
import nl.nlportal.klant.generiek.client.OpenKlantClientConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import nl.nlportal.klant.generiek.client.OpenKlantClientProvider
import nl.nlportal.klant.generiek.validation.GraphQlValidator
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
        openKlantClient: OpenKlantClient,
    ): BurgerService {
        return nl.nlportal.klant.service.impl.BurgerService(openKlantClientConfig, openKlantClient)
    }

    @Bean
    @ConditionalOnMissingBean(OpenKlantClient::class)
    fun openKlantClient(
        openKlantClientProvider: OpenKlantClientProvider,
    ): OpenKlantClient {
        return OpenKlantClient(openKlantClientProvider)
    }

    @Bean
    fun burgerQuery(burgerService: BurgerService): BurgerQuery {
        return BurgerQuery(burgerService)
    }

    @Bean
    fun burgerMutation(burgerService: BurgerService, graphQlValidator: GraphQlValidator): BurgerMutation {
        return BurgerMutation(burgerService, graphQlValidator)
    }
}