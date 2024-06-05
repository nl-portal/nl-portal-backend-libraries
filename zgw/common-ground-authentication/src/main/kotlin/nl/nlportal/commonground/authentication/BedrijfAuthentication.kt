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

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class BedrijfAuthentication(
    jwt: Jwt,
    authorities: Collection<GrantedAuthority>?,
) : CommonGroundAuthentication(jwt, authorities, KVK_NUMMER_KEY, getUserId(jwt)) {
    init {
        if (userType != KVK_NUMMER_KEY) {
            throw IllegalArgumentException("Could not create BedrijfAuthentication from this userType $userType")
        }
    }

    companion object {
        private fun getUserId(jwt: Jwt): String {
            return ((jwt.claims[AANVRAGER_KEY] as Map<*, *>?) ?: jwt.claims)[KVK_NUMMER_KEY].toString()
        }
    }

    fun getKvkNummer() = this.userId

    override fun getUserRepresentation() = "KVK:${getKvkNummer()}"
}