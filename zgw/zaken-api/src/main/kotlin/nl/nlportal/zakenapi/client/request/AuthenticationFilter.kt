package nl.nlportal.zakenapi.client.request

import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.commonground.authentication.KeycloakUserAuthentication

@Suppress("UNCHECKED_CAST")
interface AuthenticationFilter<T : AuthenticationFilter<T>> {
    fun withBsn(bsn: String): T

    fun withKvk(kvk: String): T

    fun withUid(uid: String): T

    fun withKvkAndVestigingsNummer(
        kvkNummer: String,
        vestigingsNummer: String,
    ): T

    fun withAuthentication(authentication: CommonGroundAuthentication): T {
        when (authentication) {
            is BurgerAuthentication -> this.withBsn(authentication.userId)
            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    this.withKvkAndVestigingsNummer(authentication.userId, vestigingsNummer)
                } else {
                    this.withKvk(authentication.userId)
                }
                //
            }
            is KeycloakUserAuthentication -> this.withUid(authentication.userId)
            else -> throw IllegalArgumentException("Cannot get zaken for this user")
        }

        return this as T
    }
}
