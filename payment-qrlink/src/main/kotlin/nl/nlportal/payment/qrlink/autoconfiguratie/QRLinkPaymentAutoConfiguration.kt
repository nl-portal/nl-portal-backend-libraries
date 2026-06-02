package nl.nlportal.payment.qrlink.autoconfiguratie

import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration.DirectPaymentProperties
import nl.nlportal.payment.direct.service.DirectPaymentService
import nl.nlportal.payment.qrlink.api.QRLinkPaymentController
import nl.nlportal.payment.qrlink.autoconfiguratie.QRLinkPaymentModuleConfiguration.QRLinkPaymentProperties
import nl.nlportal.payment.qrlink.filter.QRLinkPaymentAuthorizationFilter
import nl.nlportal.payment.qrlink.service.QRLinkPaymentService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableConfigurationProperties(QRLinkPaymentModuleConfiguration::class, DirectPaymentModuleConfiguration::class)
@Import(DirectPaymentService::class)
@ConditionalOnProperty(prefix = "nl-portal.config.payment", name = ["qrlink.enabled", "direct.enabled"], havingValue = "true")
class QRLinkPaymentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(QRLinkPaymentService::class)
    fun qrlinkPaymentService(
        directPaymentService: DirectPaymentService,
        qRLinkPaymentModuleConfiguration: QRLinkPaymentModuleConfiguration,
        directPaymentModuleConfiguration: DirectPaymentModuleConfiguration
    ): QRLinkPaymentService = QRLinkPaymentService(
        directPaymentService = directPaymentService,
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
    fun qrLinkPaymentAuthorizationFilter() = QRLinkPaymentAuthorizationFilter()
}
