package nl.nlportal.commonground.authentication

data class Keycloak(
    var resource: String = "",
    var credentials: Credentials = Credentials("")
    )

data class Credentials(
    var secret: String = ""
)
