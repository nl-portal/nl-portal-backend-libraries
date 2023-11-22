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
package nl.nlportal.task.autoconfigure

import nl.nlportal.case.service.CaseService
import nl.nlportal.task.graphql.CompletePublicTaskMutation
import nl.nlportal.task.graphql.CompleteTaskMutation
import nl.nlportal.task.graphql.PublicTaskQuery
import nl.nlportal.task.graphql.TaskQuery
import nl.nlportal.task.service.TaskService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
class GraphqlAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaskQuery::class)
    fun taskQuery(taskService: TaskService, caseService: CaseService): TaskQuery {
        return TaskQuery(taskService, caseService)
    }

    @Bean
    @ConditionalOnMissingBean(PublicTaskQuery::class)
    fun publicTaskQuery(taskService: TaskService, caseService: CaseService): PublicTaskQuery {
        return PublicTaskQuery(taskService)
    }

    @Bean
    @ConditionalOnMissingBean(CompleteTaskMutation::class)
    fun completeTaskMutation(taskService: TaskService, caseService: CaseService): CompleteTaskMutation {
        return CompleteTaskMutation(taskService, caseService)
    }

    @Bean
    @ConditionalOnMissingBean(CompletePublicTaskMutation::class)
    fun completePublicTaskMutation(taskService: TaskService, caseService: CaseService): CompletePublicTaskMutation {
        return CompletePublicTaskMutation(taskService, caseService)
    }
}