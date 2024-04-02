package nl.nlportal.commonground.authentication

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak")
data class Keycloak(
    var resource: String = "",
    var credentials: Credentials = Credentials("")
    )

data class Credentials(
    var secret: String = ""
)
