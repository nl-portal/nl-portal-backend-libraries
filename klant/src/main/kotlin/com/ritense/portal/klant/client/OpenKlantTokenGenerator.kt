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
package com.ritense.portal.klant.client

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.nio.charset.Charset
import java.util.Date

@Deprecated(
    message = "Moved to generic class.",
    replaceWith = ReplaceWith("IdTokenGenerator", "com.ritense.portal.idtokenauthentication.service.IdTokenGenerator")
)
class OpenKlantTokenGenerator {

    fun generateToken(secretKey: String, clientId: String, authentication: CommonGroundAuthentication): String {
        if (secretKey.length < 32) {
            throw IllegalStateException("SecretKey needs to be at least 32 in length")
        }
        val signingKey = Keys.hmacShaKeyFor(secretKey.toByteArray(Charset.forName("UTF-8")))

        val jwtBuilder = Jwts.builder()
        jwtBuilder
            .setIssuer(clientId)
            .setIssuedAt(Date())
            .claim("client_id", clientId)

        appendUserInfo(jwtBuilder, authentication)
        return jwtBuilder
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun appendUserInfo(jwtBuilder: JwtBuilder, authentication: CommonGroundAuthentication): JwtBuilder {
        return jwtBuilder
            .claim("user_id", authentication.getUserId())
            .claim("user_representation", authentication.getUserRepresentation())
    }
}