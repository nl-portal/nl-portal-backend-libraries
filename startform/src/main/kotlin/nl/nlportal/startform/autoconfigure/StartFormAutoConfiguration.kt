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
package nl.nlportal.startform.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import nl.nlportal.data.liquibase.LiquibaseMasterChangeLogLocation
import nl.nlportal.startform.autodeployment.StartFormApplicationReadyEventListener
import nl.nlportal.startform.autodeployment.StartFormDeploymentService
import nl.nlportal.startform.repository.StartFormRepository
import nl.nlportal.startform.service.StartFormService
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["nl.nlportal.startform.repository"])
@EntityScan("nl.nlportal.startform.domain")
@EnableConfigurationProperties(StartFormConfig::class)
class StartFormAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(StartFormService::class)
    fun startFormService(
        startFormConfig: StartFormConfig,
        objectenApiService: ObjectenApiService,
        startFormRepository: StartFormRepository,
        objectMapper: ObjectMapper,
    ): StartFormService {
        return StartFormService(objectMapper, objectenApiService, startFormConfig, startFormRepository)
    }

    @Bean
    @ConditionalOnMissingBean(StartFormDeploymentService::class)
    fun startFormDeploymentService(
        startFormService: StartFormService,
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper,
    ): StartFormDeploymentService {
        return StartFormDeploymentService(objectMapper, startFormService, resourceLoader)
    }

    @Bean
    @ConditionalOnMissingBean(StartFormApplicationReadyEventListener::class)
    fun startFormApplicationReadyEventListener(
        startFormDeploymentService: StartFormDeploymentService,
    ): StartFormApplicationReadyEventListener {
        return StartFormApplicationReadyEventListener(startFormDeploymentService)
    }

    @Bean
    fun startFormLiquibaseConfig(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/form-master.xml")
    }
}