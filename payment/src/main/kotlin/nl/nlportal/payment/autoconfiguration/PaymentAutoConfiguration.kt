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

import nl.nlportal.payment.api.OgonePaymentController
import nl.nlportal.payment.graphql.OgonePaymentMutation
import nl.nlportal.payment.service.OgonePaymentService
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OgonePaymentConfig::class)
class PaymentAutoConfiguration {
    @Bean
    fun ogonePaymentService(
        ogonePaymentConfig: OgonePaymentConfig,
        objectsApiClient: ObjectsApiClient,
        taakObjectConfig: TaakObjectConfig,
    ): OgonePaymentService {
        return OgonePaymentService(
            ogonePaymentConfig,
            taakObjectConfig,
            objectsApiClient,
        )
    }

    @Bean
    fun ogonePaymentMutation(ogonePaymentService: OgonePaymentService): OgonePaymentMutation {
        return OgonePaymentMutation(ogonePaymentService)
    }

    @Bean
    fun ogonePaymentController(ogonePaymentService: OgonePaymentService): OgonePaymentController {
        return OgonePaymentController(ogonePaymentService)
    }
}