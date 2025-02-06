package nl.nlportal.besluiten.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nl-portal.config.besluitenapi")
data class BesluitenApiConfig(
    var url: String = "",
    var clientId: String = "",
    var secret: String = "",
)