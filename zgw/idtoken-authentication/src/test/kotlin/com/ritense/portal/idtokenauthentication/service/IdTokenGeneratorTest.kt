package com.ritense.portal.idtokenauthentication.service

import com.ritense.portal.idtokenauthentication.TestHelper.TEST_CLIENT_ID
import com.ritense.portal.idtokenauthentication.TestHelper.TEST_ENCRYPTION_SECRET_INVALID
import com.ritense.portal.idtokenauthentication.TestHelper.TEST_ENCRYPTION_SECRET_VALID
import com.ritense.portal.idtokenauthentication.TestHelper.TEST_USER_ID
import com.ritense.portal.idtokenauthentication.TestHelper.TEST_USER_REPRESENTATION
import io.jsonwebtoken.Jwts
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdTokenGeneratorTest {
    private val idTokenGenerator = IdTokenGenerator()
    private lateinit var testClientId: String
    private lateinit var testUserId: String
    private lateinit var testUserRepresentation: Any

    @BeforeEach
    fun prepareTest() {
        testClientId = TEST_CLIENT_ID
        testUserId = TEST_USER_ID
        testUserRepresentation = TEST_USER_REPRESENTATION
    }

    @Test
    fun `should generate token with default user information`() {
        val testSecretKey = TEST_ENCRYPTION_SECRET_VALID

        val generatedToken = idTokenGenerator.generateToken(
            testSecretKey,
            testClientId
        )

        val claims = Jwts.parserBuilder()
            .setSigningKey(testSecretKey.encodeToByteArray())
            .build()
            .parseClaimsJws(generatedToken)

        Assertions.assertThat(claims.body.issuer).isEqualTo(testClientId)
        Assertions.assertThat(claims.body.get("client_id")).isEqualTo(testClientId)
        Assertions.assertThat(claims.body.get("user_id")).isEqualTo("Valtimo")
        Assertions.assertThat(claims.body.get("user_representation")).isEqualTo("Valtimo")
    }

    @Test
    fun `should generate token with custom user information`() {
        val testSecretKey = TEST_ENCRYPTION_SECRET_VALID

        val generatedToken = idTokenGenerator.generateToken(
            testSecretKey,
            testClientId,
            testUserId,
            testUserRepresentation
        )

        val claims = Jwts.parserBuilder()
            .setSigningKey(testSecretKey.encodeToByteArray())
            .build()
            .parseClaimsJws(generatedToken)

        Assertions.assertThat(claims.body.issuer).isEqualTo(testClientId)
        Assertions.assertThat(claims.body.get("client_id")).isEqualTo(testClientId)
        Assertions.assertThat(claims.body.get("user_id")).isEqualTo(testUserId)
        Assertions.assertThat(claims.body.get("user_representation")).isEqualTo(testUserRepresentation)
    }

    @Test
    fun `should throw error when secret is too short`() {
        val testSecretKey = TEST_ENCRYPTION_SECRET_INVALID

        val exception = assertThrows(IllegalArgumentException::class.java) {
            idTokenGenerator.generateToken(
                testSecretKey,
                testClientId
            )
        }

        assertEquals("SecretKey needs to be at least 32 in length", exception.message)
    }
}