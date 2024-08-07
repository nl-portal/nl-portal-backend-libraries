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
package nl.nlportal.zgw.taak.autoconfigure

import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.taak.graphql.TaakMutation
import nl.nlportal.zgw.taak.graphql.TaakMutationV2
import nl.nlportal.zgw.taak.graphql.TaakQuery
import nl.nlportal.zgw.taak.graphql.TaakQueryV2
import nl.nlportal.zgw.taak.service.TaakService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(TaakObjectConfig::class)
class TaakAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(TaakService::class)
    fun taskService(
        objectsApiClient: ObjectsApiClient,
        taakObjectConfig: TaakObjectConfig,
    ): TaakService {
        return TaakService(objectsApiClient, taakObjectConfig)
    }

    @Bean
    @ConditionalOnMissingBean(TaakQuery::class)
    fun taskQuery(taskService: TaakService): TaakQuery {
        return TaakQuery(taskService)
    }

    @Bean
    @ConditionalOnMissingBean(TaakMutation::class)
    fun taskMutation(taskService: TaakService): TaakMutation {
        return TaakMutation(taskService)
    }

    @Bean
    @ConditionalOnMissingBean(TaakQueryV2::class)
    fun taskQueryV2(taskService: TaakService): TaakQueryV2 {
        return TaakQueryV2(taskService)
    }

    @Bean
    @ConditionalOnMissingBean(TaakMutationV2::class)
    fun taskMutationV2(taskService: TaakService): TaakMutationV2 {
        return TaakMutationV2(taskService)
    }
}