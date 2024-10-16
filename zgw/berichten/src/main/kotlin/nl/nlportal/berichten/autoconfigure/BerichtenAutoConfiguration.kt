/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.autoconfigure

import nl.nlportal.berichten.graphql.BerichtenQuery
import nl.nlportal.berichten.service.BerichtenService
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    BerichtenConfigurationProperties::class,
)
class BerichtenAutoConfiguration {
    @Bean
    fun berichtenService(
        objectenApiService: ObjectenApiService,
        berichtenConfigurationProperties: BerichtenConfigurationProperties,
    ): BerichtenService {
        return BerichtenService(objectenApiService, berichtenConfigurationProperties)
    }

    @Bean
    @ConditionalOnMissingBean(BerichtenQuery::class)
    fun berichtenQuery(berichtenService: BerichtenService): BerichtenQuery {
        return BerichtenQuery(berichtenService)
    }
}