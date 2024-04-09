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
package nl.nlportal.payment.autoconfiguration

import nl.nlportal.payment.api.PaymentController
import nl.nlportal.payment.graphql.PaymentMutation
import nl.nlportal.payment.service.PaymentService
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(PaymentConfig::class)
class PaymentAutoConfiguration {
    @Bean
    fun paymentService(
        paymentConfig: PaymentConfig,
        objectsApiClient: ObjectsApiClient,
    ): PaymentService {
        return PaymentService(
            paymentConfig,
            objectsApiClient,
        )
    }

    @Bean
    fun paymentMutation(paymentService: PaymentService): PaymentMutation {
        return PaymentMutation(paymentService)
    }

    @Bean
    fun paymentController(paymentService: PaymentService): PaymentController {
        return PaymentController(paymentService)
    }
}