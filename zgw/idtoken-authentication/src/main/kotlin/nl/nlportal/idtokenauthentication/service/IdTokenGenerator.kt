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
package nl.nlportal.idtokenauthentication.service

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*

class IdTokenGenerator {
    fun generateToken(
        secretKey: String,
        clientId: String,
    ): String {
        require(secretKey.length >= 32) {
            "SecretKey needs to be at least 32 in length"
        }

        val signingKey = Keys.hmacShaKeyFor(secretKey.encodeToByteArray())
        val jwtBuilder = Jwts.builder()

        return jwtBuilder
            .issuer(clientId)
            .issuedAt(Date())
            .claim("client_id", clientId)
            .appendUserInfo(null, null)
            .signWith(signingKey)
            .compact()
    }

    fun generateToken(
        secretKey: String,
        clientId: String,
        userId: String,
        userRepresentation: Any,
    ): String {
        require(secretKey.length >= 32) {
            "SecretKey needs to be at least 32 in length"
        }

        val signingKey = Keys.hmacShaKeyFor(secretKey.encodeToByteArray())
        val jwtBuilder = Jwts.builder()

        return jwtBuilder
            .issuer(clientId)
            .issuedAt(Date())
            .claim("client_id", clientId)
            .appendUserInfo(userId, userRepresentation)
            .signWith(signingKey)
            .compact()
    }

    fun generateToken(
        secretKey: String,
        clientId: String,
        claims: Map<String, Any>,
    ): String {
        require(secretKey.length >= 32) {
            "SecretKey needs to be at least 32 in length"
        }

        val signingKey = Keys.hmacShaKeyFor(secretKey.encodeToByteArray())
        val jwtBuilder = Jwts.builder()

        return jwtBuilder
            .issuer(clientId)
            .issuedAt(Date())
            .claim("client_id", clientId)
            .claims(claims)
            .appendUserInfo(null, null)
            .signWith(signingKey)
            .compact()
    }

    fun generateToken(
        secretKey: String,
        clientId: String,
        userId: String,
        userRepresentation: Any,
        claims: Map<String, Any>,
    ): String {
        require(secretKey.length >= 32) {
            "SecretKey needs to be at least 32 in length"
        }

        val signingKey = Keys.hmacShaKeyFor(secretKey.encodeToByteArray())
        val jwtBuilder = Jwts.builder()

        return jwtBuilder
            .issuer(clientId)
            .issuedAt(Date())
            .claims(claims)
            .appendUserInfo(userId, userRepresentation)
            .signWith(signingKey)
            .compact()
    }

    private fun JwtBuilder.appendUserInfo(
        userId: String?,
        userRepresentation: Any?,
    ): JwtBuilder {
        return this
            .claim("user_id", userId ?: DEFAULT_USER_ID)
            .claim("user_representation", userRepresentation ?: DEFAULT_USER_REPRESENTATION)
    }

    companion object {
        private const val DEFAULT_USER_ID = "Valtimo"
        private const val DEFAULT_USER_REPRESENTATION = "Valtimo"
    }
}