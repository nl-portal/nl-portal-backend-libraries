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

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet.Builder
import com.nimbusds.jwt.SignedJWT
import java.util.Date


class IdTokenGenerator {
    fun generateToken(
        secretKey: String,
        clientId: String,
    ): String {
        require(secretKey.length >= 32) {
            "SecretKey needs to be at least 32 in length"
        }

        val claimsSet = Builder()
            .subject(clientId)
            .issuer(clientId)
            .issueTime(Date())
            .expirationTime(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutes
            .claim("client_id", clientId)
            .appendUserInfo(null, null)
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.HS256)
                .build(),
            claimsSet
        )
        signedJWT.sign(MACSigner(secretKey.encodeToByteArray()))
        return signedJWT.serialize()
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

        val claimsSet = Builder()
            .subject(clientId)
            .issuer(clientId)
            .issueTime(Date())
            .expirationTime(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutes
            .claim("client_id", clientId)
            .appendUserInfo(userId, userRepresentation)
            .build()

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.HS256)
                .build(),
            claimsSet
        )
        signedJWT.sign(MACSigner(secretKey.encodeToByteArray()))
        return signedJWT.serialize()
    }

    private fun Builder.appendUserInfo(
        userId: String?,
        userRepresentation: Any?,
    ): Builder {
        return this
            .claim("user_id", userId ?: DEFAULT_USER_ID)
            .claim("user_representation", userRepresentation ?: DEFAULT_USER_REPRESENTATION)
    }

    companion object {
        private const val DEFAULT_USER_ID = "Valtimo"
        private const val DEFAULT_USER_REPRESENTATION = "Valtimo"
    }
}
