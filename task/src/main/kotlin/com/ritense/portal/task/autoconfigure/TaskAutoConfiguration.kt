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
package com.ritense.portal.task.autoconfigure

import com.ritense.portal.case.service.CaseService
import com.ritense.portal.data.liquibase.LiquibaseMasterChangeLogLocation
import com.ritense.portal.messaging.`in`.CreatePortalTaskMessage
import com.ritense.portal.messaging.`in`.DeletePortalTaskMessage
import com.ritense.portal.messaging.out.PortalMessage
import com.ritense.portal.task.repository.TaskRepository
import com.ritense.portal.task.service.TaskService
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import reactor.core.publisher.Sinks
import java.util.function.Consumer

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.portal.task.repository"])
@EntityScan("com.ritense.portal.task.domain")
class TaskAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaskService::class)
    fun taskService(
        taskRepository: TaskRepository,
        sink: Sinks.Many<PortalMessage>,
        caseService: CaseService
    ): TaskService {
        return TaskService(taskRepository, sink, caseService)
    }

    // Consumers
    @Bean
    fun createPortalTaskConsumer(taskService: TaskService): Consumer<CreatePortalTaskMessage>? {
        return Consumer<CreatePortalTaskMessage> { message: CreatePortalTaskMessage ->
            run {
                logger.info { "Received case id: ${message.externalCaseId} with form def: ${message.formDefinition}" }
                taskService.createPortalTask(message)
            }
        }
    }

    @Bean
    fun deletePortalTaskConsumer(taskService: TaskService): Consumer<DeletePortalTaskMessage>? {
        return Consumer<DeletePortalTaskMessage> { message: DeletePortalTaskMessage ->
            run {
                logger.info { "Received delete message for external task id: ${message.taskId}" }
                taskService.deletePortalTask(message)
            }
        }
    }

    @Bean
    fun taskLiquibaseConfig(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/task-master.xml")
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}