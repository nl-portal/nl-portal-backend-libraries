/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.idtokenauthentication.service

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.Date

class IdTokenGenerator {

    fun generateToken(secretKey: String, clientId: String): String {
        require(secretKey.length >= 32) {
            "SecretKey needs to be at least 32 in length"
        }

        val signingKey = Keys.hmacShaKeyFor(secretKey.encodeToByteArray())
        val jwtBuilder = Jwts.builder()

        return jwtBuilder
            .setIssuer(clientId)
            .setIssuedAt(Date())
            .claim("client_id", clientId)
            .appendUserInfo()
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun JwtBuilder.appendUserInfo(): JwtBuilder {
        return this
            .claim("user_id", DEFAULT_USER_ID)
            .claim("user_representation", "")
    }

    companion object {
        private const val DEFAULT_USER_ID = "Valtimo"
    }
}