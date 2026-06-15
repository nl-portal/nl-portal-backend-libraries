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
package nl.nlportal.payment.qrlink.autoconfiguratie

import nl.nlportal.core.qrcode.QRCodeService
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import nl.nlportal.payment.direct.service.DirectPaymentService
import nl.nlportal.payment.qrlink.web.rest.QRLinkPaymentController
import nl.nlportal.payment.qrlink.filter.QRLinkPaymentAuthorizationFilter
import nl.nlportal.payment.qrlink.service.QRLinkPaymentService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(QRLinkPaymentModuleConfiguration::class, DirectPaymentModuleConfiguration::class)
@ConditionalOnProperty(prefix = "nl-portal.config.payment", name = ["qrlink.enabled", "direct.enabled"], havingValue = "true")
class QRLinkPaymentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(QRLinkPaymentService::class)
    fun qrlinkPaymentService(
        directPaymentService: DirectPaymentService,
        qrCodeService: QRCodeService,
        qRLinkPaymentModuleConfiguration: QRLinkPaymentModuleConfiguration,
        directPaymentModuleConfiguration: DirectPaymentModuleConfiguration
    ): QRLinkPaymentService = QRLinkPaymentService(
        directPaymentService = directPaymentService,
        qrCodeService = qrCodeService,
        qrLinkPaymentProperties = qRLinkPaymentModuleConfiguration.properties,
        directPaymentProperties = directPaymentModuleConfiguration.properties
    )

    @Bean
    @ConditionalOnMissingBean(QRLinkPaymentController::class)
    fun qrLinkPaymentController(
        qrLinkPaymentService: QRLinkPaymentService,
    ) = QRLinkPaymentController(qrLinkPaymentService = qrLinkPaymentService)

    @Bean
    @ConditionalOnMissingBean(QRLinkPaymentAuthorizationFilter::class)
    fun qrLinkPaymentAuthorizationFilter(
        qRLinkPaymentModuleConfiguration: QRLinkPaymentModuleConfiguration,
    ) = QRLinkPaymentAuthorizationFilter(
        qrLinkPaymentProperties = qRLinkPaymentModuleConfiguration.properties,
    )
}
