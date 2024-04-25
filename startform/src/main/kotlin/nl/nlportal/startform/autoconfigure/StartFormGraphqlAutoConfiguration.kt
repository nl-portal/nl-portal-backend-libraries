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

import nl.nlportal.startform.graphql.CreateStartFormMutation
import nl.nlportal.startform.graphql.StartFormQuery
import nl.nlportal.startform.service.StartFormService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class StartFormGraphqlAutoConfiguration {
    @Bean
    fun startFromQuery(startFormService: StartFormService): StartFormQuery {
        return StartFormQuery(startFormService)
    }

    @Bean
    fun createStartFormMutation(startFormService: StartFormService): CreateStartFormMutation {
        return CreateStartFormMutation(startFormService)
    }
}