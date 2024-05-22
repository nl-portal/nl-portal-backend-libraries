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
package nl.nlportal.portal.authentication.domain

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

open class PortalAuthentication(
    open val jwt: Jwt,
    authorities: Collection<GrantedAuthority>?,
) : JwtAuthenticationToken(jwt, authorities) {
    open fun getGemachtigde(): AuthenticationGemachtigde? {
        return AuthenticationGemachtigde(
            jwt.claims[SUB_KEY]?.toString(),
        )
    }

    open fun getUserId() = "Portal"

    open fun getUserRepresentation() = "Portal"
}

const val SUB_KEY = "sub"