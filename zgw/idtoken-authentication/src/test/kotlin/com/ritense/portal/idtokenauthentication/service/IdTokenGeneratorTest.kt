package com.ritense.portal.idtokenauthentication.service

import io.jsonwebtoken.Jwts
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IdTokenGeneratorTest {
    private val idTokenGenerator = IdTokenGenerator()

    @Test
    fun `should generate token`() {
        val testSecretKey = "NYy.TAQYbHRRWC_b2rfoqs4oR9jXj38N"

        val generatedToken = idTokenGenerator.generateToken(
            testSecretKey,
            "testClientId"
        )

        val claims = Jwts.parserBuilder()
            .setSigningKey(testSecretKey.encodeToByteArray())
            .build()
            .parseClaimsJws(generatedToken)

        Assertions.assertThat(claims.body.issuer).isEqualTo("testClientId")
        Assertions.assertThat(claims.body.get("client_id")).isEqualTo("testClientId")
        Assertions.assertThat(claims.body.get("user_id")).isEqualTo("Valtimo")
        Assertions.assertThat(claims.body.get("user_representation")).isEqualTo("")
    }

    @Test
    fun `should throw error when secret is too short`() {
        val testSecretKey = "RRWC_b2rfXj38Noqs4o.R9j"

        val exception = assertThrows(IllegalArgumentException::class.java) {
            idTokenGenerator.generateToken(
                testSecretKey,
                "testClientId"
            )
        }

        assertEquals("SecretKey needs to be at least 32 in length", exception.message)
    }
}