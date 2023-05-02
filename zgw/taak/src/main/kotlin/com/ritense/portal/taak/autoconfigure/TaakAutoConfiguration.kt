/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.taak.autoconfigure

import com.ritense.portal.gzac.objectsapi.client.ObjectsApiClient
import com.ritense.portal.taak.graphql.TaakMutation
import com.ritense.portal.taak.graphql.TaakQuery
import com.ritense.portal.taak.service.TaakService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TaakAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaakService::class)
    fun gzacTaskService(
        objectsApiClient: ObjectsApiClient,
        objectsApiTaskConfig: ObjectsApiTaakConfig
    ): TaakService {
        return TaakService(objectsApiClient, objectsApiTaskConfig)
    }

    @Bean
    @ConditionalOnMissingBean(TaakQuery::class)
    fun gzacTaskQuery(taskService: TaakService): TaakQuery {
        return TaakQuery(taskService)
    }

    @Bean
    @ConditionalOnMissingBean(TaakMutation::class)
    fun gzacTaskMutation(taskService: TaakService): TaakMutation {
        return TaakMutation(taskService)
    }
}