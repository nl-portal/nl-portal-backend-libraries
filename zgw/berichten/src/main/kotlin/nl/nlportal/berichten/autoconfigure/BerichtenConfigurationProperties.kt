package nl.nlportal.berichten.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.zgw.berichten")
data class BerichtenConfigurationProperties(
    val berichtObjectTypeUrl: String,
)