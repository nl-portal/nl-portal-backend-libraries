package nl.nlportal.payment.qrlink.autoconfiguratie

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.payment.qrlink", ignoreUnknownFields = true)
data class QRLinkPaymentModuleConfiguration(
    var enabled: Boolean = false,
    var properties: QRLinkPaymentProperties = QRLinkPaymentProperties(),
) {
    data class QRLinkPaymentProperties(
        val landingUrl: String = "",
        val returnUrl: String = "",
        val qrcodeHeight: Int = 0,
        val qrcodeWidth: Int = 0,
        var secret: String = "",
    )
}
