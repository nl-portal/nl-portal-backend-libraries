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

import nl.nlportal.payment.direct.api.OgoneDirectPaymentController
import nl.nlportal.payment.direct.graphql.OgoneDirectPaymentMutation
import nl.nlportal.payment.direct.service.OgoneDirectPaymentService
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OgoneDirectPaymentModuleConfiguration::class)
@ConditionalOnProperty(prefix = "nl-portal.config.payment.direct.ogone", name = ["enabled"], havingValue = "true")
class OgoneDirectPaymentAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(OgoneDirectPaymentService::class)
    fun ogonePaymentDirectService(
        ogoneDirectPaymentModuleConfiguration: OgoneDirectPaymentModuleConfiguration,
        objectsApiClient: ObjectsApiClient,
    ): OgoneDirectPaymentService {
        return OgoneDirectPaymentService(
            ogoneDirectPaymentModuleConfiguration,
            objectsApiClient,
        )
    }

    @Bean
    fun ogonePaymentDirectMutation(ogoneDirectPaymentService: OgoneDirectPaymentService): OgoneDirectPaymentMutation {
        return OgoneDirectPaymentMutation(ogoneDirectPaymentService)
    }

    @Bean
    @ConditionalOnMissingBean(OgoneDirectPaymentController::class)
    fun ogonePaymentDirectController(ogoneDirectPaymentService: OgoneDirectPaymentService): OgoneDirectPaymentController {
        return OgoneDirectPaymentController(ogoneDirectPaymentService)
    }
}