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
package nl.nlportal.case.autoconfigure

import nl.nlportal.case.autodeployment.CaseDefinitionApplicationReadyEventListener
import nl.nlportal.case.repository.CaseDefinitionRepository
import nl.nlportal.case.repository.CaseRepository
import nl.nlportal.case.service.CaseDefinitionService
import nl.nlportal.case.service.CaseService
import nl.nlportal.data.liquibase.LiquibaseMasterChangeLogLocation
import nl.nlportal.messaging.`in`.UpdateExternalIdPortalCaseMessage
import nl.nlportal.messaging.`in`.UpdatePortalCaseMessage
import nl.nlportal.messaging.`in`.UpdateStatusPortalCaseMessage
import nl.nlportal.messaging.out.PortalMessage
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import reactor.core.publisher.Sinks
import java.util.function.Consumer

@Configuration
@EnableJpaRepositories(basePackages = ["nl.nlportal.case.repository"])
@EntityScan("nl.nlportal.case.domain")
class CaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionService::class)
    fun caseDefinitionService(
        caseDefinitionRepository: CaseDefinitionRepository,
        resourceLoader: ResourceLoader,
    ): CaseDefinitionService {
        return CaseDefinitionService(caseDefinitionRepository, resourceLoader)
    }

    @Bean
    @ConditionalOnMissingBean(CaseService::class)
    fun caseService(
        caseRepository: CaseRepository,
        caseDefinitionService: CaseDefinitionService,
        sink: Sinks.Many<PortalMessage>,
    ): CaseService {
        return CaseService(caseRepository, caseDefinitionService, sink)
    }

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionApplicationReadyEventListener::class)
    fun caseDefinitionApplicationReadyEventListener(
        caseDefinitionService: CaseDefinitionService,
    ): CaseDefinitionApplicationReadyEventListener {
        return CaseDefinitionApplicationReadyEventListener(caseDefinitionService)
    }

    // Consumers
    @Bean
    fun updateExternalIdPortalCaseConsumer(caseService: CaseService): Consumer<UpdateExternalIdPortalCaseMessage>? {
        return Consumer<UpdateExternalIdPortalCaseMessage> { message: UpdateExternalIdPortalCaseMessage ->
            run {
                logger.info { "Received case id: ${message.caseId} with external id: ${message.externalId}" }
                caseService.updateExternalId(message)
            }
        }
    }

    @Bean
    fun updateStatusPortalCaseConsumer(caseService: CaseService): Consumer<UpdateStatusPortalCaseMessage>? {
        return Consumer<UpdateStatusPortalCaseMessage> { message: UpdateStatusPortalCaseMessage ->
            run {
                logger.info { "Received status update with status: ${message.status} with external id: ${message.externalId}" }
                caseService.updateStatus(message)
            }
        }
    }

    @Bean
    fun updatePortalCaseConsumer(caseService: CaseService): Consumer<UpdatePortalCaseMessage>? {
        return Consumer<UpdatePortalCaseMessage> { message: UpdatePortalCaseMessage ->
            run {
                logger.info { "Received case update with external id: ${message.externalId}" }
                caseService.updateCase(message)
            }
        }
    }

    @Bean
    fun caseLiquibaseConfig(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/case-master.xml")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}