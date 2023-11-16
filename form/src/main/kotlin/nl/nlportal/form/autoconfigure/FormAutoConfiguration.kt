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
package nl.nlportal.form.autoconfigure

import nl.nlportal.data.liquibase.LiquibaseMasterChangeLogLocation
import nl.nlportal.form.autodeployment.FormApplicationReadyEventListener
import nl.nlportal.form.autodeployment.FormDefinitionDeploymentService
import nl.nlportal.form.repository.FormIoFormDefinitionRepository
import nl.nlportal.form.service.FormIoFormDefinitionService
import nl.nlportal.form.service.ObjectsApiFormDefinitionService
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["nl.nlportal.form.repository"])
@EntityScan("nl.nlportal.form.domain")
class FormAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FormApplicationReadyEventListener::class)
    fun formApplicationReadyEventListener(
        formDefinitionDeploymentService: FormDefinitionDeploymentService,
    ): FormApplicationReadyEventListener {
        return FormApplicationReadyEventListener(formDefinitionDeploymentService)
    }

    @Bean
    @ConditionalOnMissingBean(FormIoFormDefinitionService::class)
    fun formIoFormDefinitionService(
        formIoFormDefinitionRepository: FormIoFormDefinitionRepository,
    ): FormIoFormDefinitionService {
        return FormIoFormDefinitionService(formIoFormDefinitionRepository)
    }

    @Bean
    @ConditionalOnMissingBean(ObjectsApiFormDefinitionService::class)
    fun objectsApiFormDefinitionService(
        objectenApiService: ObjectenApiService,
    ): ObjectsApiFormDefinitionService {
        return ObjectsApiFormDefinitionService(objectenApiService)
    }

    @Bean
    @ConditionalOnMissingBean(FormDefinitionDeploymentService::class)
    fun formDefinitionDeploymentService(
        formIoFormDefinitionService: FormIoFormDefinitionService,
        resourceLoader: ResourceLoader,
    ): FormDefinitionDeploymentService {
        return FormDefinitionDeploymentService(formIoFormDefinitionService, resourceLoader)
    }

    @Bean
    fun formLiquibaseConfig(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/form-master.xml")
    }
}