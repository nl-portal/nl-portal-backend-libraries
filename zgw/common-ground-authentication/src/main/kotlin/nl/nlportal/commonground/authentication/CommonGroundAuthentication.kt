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
package nl.nlportal.commonground.authentication

import nl.nlportal.portal.authentication.domain.PortalAuthentication
import nl.nlportal.portal.authentication.domain.SUB_KEY
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

abstract class CommonGroundAuthentication(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>?,
    userId: String,
    userType: String,
) : PortalAuthentication(jwt, authorities, userId, userType) {
    /**
     * Gets gemachtigde identification property from the JWT
     *
     * @return Gemachtigde
     */
    fun getGemachtigde(): AuthenticationGemachtigde? {
        if (token.claims[GEMACHTIGDE_KEY] == null) {
            return null
        }

        return (token.claims[GEMACHTIGDE_KEY] as Map<*, *>).let {
            AuthenticationGemachtigde(
                it[BSN_KEY]?.toString(),
                it[KVK_NUMMER_KEY]?.toString(),
                it[SUB_KEY]?.toString(),
            )
        }
    }

    /**
     * Gets VestigingsNummer property from the JWT
     *
     * @return VestigingsNummer
     */
    fun getVestigingsNummer(): String? = ((token.claims[AANVRAGER_KEY] as Map<*, *>?) ?: token.claims)[VESTIGINGNUMMER_KEY]?.toString()

    /**
     * Gets MachtingsDienst UUIDs property from the JWT
     * param: portalMachtigingUuid: portal uuid which indicates the user has all the machtigingen
     *
     * @return MachtingsDienst UUIDs
     */
    fun machtigingsDienstUUIDs(allMachtigingUuid: UUID? = null): List<UUID>? {
        if (token.claims[MACHTIGINGSDIENST_KEY] == null) {
            return null
        }
        val claim = token.claims[MACHTIGINGSDIENST_KEY]
        val list =
            when (claim) {
                is List<*> -> {
                    claim.filterIsInstance<String>().map { UUID.fromString(it) }
                }
                is String -> {
                    listOf(UUID.fromString(claim))
                }
                else -> null
            }

        if (list == null) {
            return null
        }

        return if (list.contains(allMachtigingUuid)) {
            null
        } else {
            list
        }
    }

    /**
     * Gets MachtingsDienst UUID property from the JWT
     *
     * @return MachtingsDienst UUID
     */
    @Deprecated(
        """
            Will be removed in a future version.
            Use machtigingsDienstUUIDs which has support for multiple machtigingen  
        """,
    )
    fun machtigingsDienstUUID(): UUID? = machtigingsDienstUUIDs()?.get(0)

    override fun getUserRepresentation() = "${this.userType.uppercase()}:${this.userId}"
}

const val BSN_KEY = "bsn"
const val KVK_NUMMER_KEY = "kvk"
const val AANVRAGER_KEY = "aanvrager"
const val VESTIGINGNUMMER_KEY = "vestigingsnummer"
const val GEMACHTIGDE_KEY = "gemachtigde"
const val MACHTIGINGSDIENST_KEY = "urn:etoegang:core:ServiceUUID"