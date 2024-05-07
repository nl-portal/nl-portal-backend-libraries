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
package nl.nlportal.portal.authentication

import nl.nlportal.portal.authentication.domain.SUB_KEY
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID

class JwtBuilder {
    private var jwtBuilder: Jwt.Builder =
        Jwt
            .withTokenValue("token")
            .header("alg", "none")

    fun buildJwt(): Jwt {
        jwtBuilder.claim("claims", "claim")
        return jwtBuilder.build()
    }

    fun addKeycloakUUIDToClaims(uuid: UUID): Jwt.Builder {
        jwtBuilder.claim(SUB_KEY, uuid)
        return jwtBuilder
    }
}