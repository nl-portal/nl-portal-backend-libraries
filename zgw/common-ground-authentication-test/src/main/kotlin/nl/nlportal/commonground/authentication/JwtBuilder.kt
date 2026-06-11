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

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet.Builder
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID
import nl.nlportal.portal.authentication.domain.SUB_KEY
import org.springframework.security.oauth2.jwt.Jwt

class JwtBuilder {
    private var aanvragerBsn: String? = null
    private var aanvragerKvk: String? = null
    private var aanvragerUid: String? = null

    private var jwtBuilder: Jwt.Builder =
        Jwt
            .withTokenValue("token")
            .header("alg", "none")

    fun aanvragerBsn(bsn: String): JwtBuilder {
        assert(aanvragerKvk == null && aanvragerUid == null, { "cannot set bsn for jwt that already has kvk" })

        val aanvrager =
            mapOf<String, Any>(
                BSN_KEY to bsn,
            )
        jwtBuilder.claim(AANVRAGER_KEY, aanvrager)
        this.aanvragerBsn = bsn

        return this
    }

    fun aanvragerKvk(kvk: String): JwtBuilder {
        assert(aanvragerBsn == null && aanvragerUid == null, { "cannot set kvk for jwt that already has bsn" })

        val aanvrager =
            mapOf<String, Any>(
                KVK_NUMMER_KEY to kvk,
            )
        jwtBuilder.claim(AANVRAGER_KEY, aanvrager)
        this.aanvragerKvk = kvk

        return this
    }

    fun aanvragerUid(uuid: UUID): JwtBuilder {
        assert(aanvragerBsn == null && aanvragerKvk == null, { "cannot set uid for jwt that already has kvk or bsn" })

        jwtBuilder.claim(SUB_KEY, uuid.toString())
        this.aanvragerUid = uuid.toString()

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

    fun vestigingsNummerKvk(
        kvk: String,
        vestigingsNummer: String,
    ): JwtBuilder {
        val aanvrager =
            mapOf<String, Any>(
                KVK_NUMMER_KEY to kvk,
                VESTIGINGNUMMER_KEY to vestigingsNummer,
            )
        jwtBuilder.claim(AANVRAGER_KEY, aanvrager)

        return this
    }

    fun machtingDienstKvk(machtingDienst: String): JwtBuilder {
        jwtBuilder.claim(
            MACHTIGINGSDIENST_KEY,
            listOf(machtingDienst),
        )

        return this
    }

    fun buildJwt(): Jwt {
        if (this.aanvragerBsn == null && this.aanvragerKvk == null && this.aanvragerUid == null) {
            throw IllegalStateException("aanvrager needs to be set with either bsn or kvk")
        }

        return jwtBuilder.build()
    }

    fun buildJwtString(secretString: String): String {
        val claimsSet =
            Builder()
                .issueTime(Date())
                .expirationTime(Date(System.currentTimeMillis() + 20000)) // 15 minutes
                .claim("claim", "value")
                .build()

        val signedJWT =
            SignedJWT(
                JWSHeader
                    .Builder(JWSAlgorithm.HS256)
                    .build(),
                claimsSet,
            )
        signedJWT.sign(MACSigner(secretString.encodeToByteArray()))
        return signedJWT.serialize()
    }

    fun buildBurgerAuthentication(): BurgerAuthentication {
        val jwt = buildJwt()
        if (this.aanvragerKvk != null || this.aanvragerUid != null) {
            throw IllegalStateException("cannot build BurgerAuthentication with kvk or uid")
        }
        return BurgerAuthentication(jwt, emptyList())
    }

    fun buildBedrijfAuthentication(): BedrijfAuthentication {
        val jwt = buildJwt()
        if (this.aanvragerBsn != null || this.aanvragerUid != null) {
            throw IllegalStateException("cannot build BedrijfAuthentication with bsn or uid")
        }
        return BedrijfAuthentication(jwt, emptyList())
    }

    fun buildKeycloakUserAuthentication(): KeycloakUserAuthentication {
        val jwt = buildJwt()
        if (this.aanvragerKvk != null || this.aanvragerBsn != null) {
            throw IllegalStateException("cannot build KeycloakUserAuthentication with bsn or kvk")
        }
        return KeycloakUserAuthentication(jwt, emptyList())
    }
}