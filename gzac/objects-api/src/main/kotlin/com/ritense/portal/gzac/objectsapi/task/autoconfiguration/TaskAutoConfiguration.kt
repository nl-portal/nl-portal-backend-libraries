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
package com.ritense.portal.gzac.objectsapi.task.autoconfiguration

import com.ritense.portal.gzac.objectsapi.client.ObjectsApiClient
import com.ritense.portal.gzac.objectsapi.task.graphql.TaskMutation
import com.ritense.portal.gzac.objectsapi.task.graphql.TaskQuery
import com.ritense.portal.gzac.objectsapi.task.service.TaskService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ObjectsApiTaskConfig::class)
class TaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaskService::class)
    fun gzacTaskService(
        objectsApiClient: ObjectsApiClient,
        objectsApiTaskConfig: ObjectsApiTaskConfig
    ): TaskService {
        return TaskService(objectsApiClient, objectsApiTaskConfig)
    }

    @Bean
    @ConditionalOnMissingBean(TaskQuery::class)
    fun gzacTaskQuery(taskService: TaskService): TaskQuery {
        return TaskQuery(taskService)
    }

    @Bean
    @ConditionalOnMissingBean(TaskMutation::class)
    fun gzacTaskMutation(taskService: TaskService): TaskMutation {
        return TaskMutation(taskService)
    }
}