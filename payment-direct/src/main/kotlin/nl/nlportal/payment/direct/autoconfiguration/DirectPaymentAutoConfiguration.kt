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
package nl.nlportal.payment.direct.autoconfiguration

import nl.nlportal.payment.direct.api.DirectPaymentController
import nl.nlportal.payment.direct.graphql.DirectPaymentMutation
import nl.nlportal.payment.direct.service.DirectPaymentService
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(DirectPaymentModuleConfiguration::class)
@ConditionalOnProperty(prefix = "nl-portal.config", name = ["objectenapi.enabled", "payment.direct.enabled"], havingValue = "true")
class DirectPaymentAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DirectPaymentService::class)
    fun directPaymentService(
        directPaymentModuleConfiguration: DirectPaymentModuleConfiguration,
        objectsApiClient: ObjectsApiClient,
    ): DirectPaymentService =
        DirectPaymentService(
            directPaymentModuleConfiguration,
            objectsApiClient,
        )

    @Bean
    fun directPaymentMutation(directPaymentService: DirectPaymentService): DirectPaymentMutation = DirectPaymentMutation(directPaymentService)

    @Bean
    @ConditionalOnMissingBean(DirectPaymentController::class)
    fun directPaymentController(directPaymentService: DirectPaymentService): DirectPaymentController = DirectPaymentController(directPaymentService)
}