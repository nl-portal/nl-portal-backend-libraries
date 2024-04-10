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

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

class JwtBuilder {
    private var aanvragerBsn: String? = null
    private var aanvragerKvk: String? = null

    private var jwtBuilder: Jwt.Builder =
        Jwt
            .withTokenValue("token")
            .header("alg", "none")

    fun aanvragerBsn(bsn: String): JwtBuilder {
        assert(aanvragerKvk == null, { "cannot set bsn for jwt that already has kvk" })

        val aanvrager =
            mapOf<String, Any>(
                BSN_KEY to bsn,
            )
        jwtBuilder.claim(AANVRAGER_KEY, aanvrager)
        this.aanvragerBsn = bsn

        return this
    }

    fun aanvragerKvk(kvk: String): JwtBuilder {
        assert(aanvragerBsn == null, { "cannot set kvk for jwt that already has bsn" })

        val aanvrager =
            mapOf<String, Any>(
                KVK_NUMMER_KEY to kvk,
            )
        jwtBuilder.claim(AANVRAGER_KEY, aanvrager)
        this.aanvragerKvk = kvk

        return this
    }

    fun gemachtigdeBsn(bsn: String): JwtBuilder {
        val gemachtigde =
            mapOf<String, Any>(
                BSN_KEY to bsn,
            )
        jwtBuilder.claim(GEMACHTIGDE_KEY, gemachtigde)

        return this
    }

    fun gemachtigdeKvk(kvk: String): JwtBuilder {
        val gemachtigde =
            mapOf<String, Any>(
                KVK_NUMMER_KEY to kvk,
            )
        jwtBuilder.claim(GEMACHTIGDE_KEY, gemachtigde)

        return this
    }

    fun buildJwt(): Jwt {
        if (this.aanvragerBsn == null && this.aanvragerKvk == null) {
            throw IllegalStateException("aanvrager needs to be set with either bsn or kvk")
        }

        return jwtBuilder.build()
    }

    fun randomClaimes(): Jwt.Builder? {
        return jwtBuilder.claim("claim", "value")
    }

    fun buildJwtString(): String {
        val jwt = randomClaimes()!!.build()

        val key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
        val base64 = Encoders.BASE64.encode(key.getEncoded())

        return Jwts.builder()
            .setClaims(jwt.claims)
            .setSubject(jwt.subject)
            .setExpiration(Date(System.currentTimeMillis() + 20000))
            .signWith(SignatureAlgorithm.HS512, base64)
            .compact()
    }

    fun buildBurgerAuthentication(): BurgerAuthentication {
        val jwt = buildJwt()
        if (this.aanvragerKvk != null) {
            throw IllegalStateException("cannot build BurgerAuthentication with kvk")
        }
        return BurgerAuthentication(jwt, emptyList())
    }

    fun buildBedrijfAuthentication(): BedrijfAuthentication {
        val jwt = buildJwt()
        if (this.aanvragerBsn != null) {
            throw IllegalStateException("cannot build BedrijfAuthentication with bsn")
        }
        return BedrijfAuthentication(jwt, emptyList())
    }
}