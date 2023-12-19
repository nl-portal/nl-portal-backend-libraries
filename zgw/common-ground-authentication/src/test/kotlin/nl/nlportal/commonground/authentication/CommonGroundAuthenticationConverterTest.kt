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

import nl.nlportal.commonground.authentication.exception.UserTypeUnsupportedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

internal class CommonGroundAuthenticationConverterTest {
    val converter = CommonGroundAuthenticationConverter()

    @Test
    fun `converter returns BurgerAuthentication when JWT has BSN`() {
        val jwt = JwtBuilder().aanvragerBsn("1234").buildJwt()

        val authentication = converter.convert(jwt)

        assertTrue(authentication.block() is BurgerAuthentication)
    }

    @Test
    fun `converter returns BedrijfAuthentication when JWT has KvK nummer`() {
        val jwt = JwtBuilder().aanvragerKvk("1234").buildJwt()

        val authentication = converter.convert(jwt)

        assertTrue(authentication.block() is BedrijfAuthentication)
    }

    @Test
    fun `converter throws exception when JWT has no KvK nummer or BSN`() {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "none")
                .claim("random", "1234")
                .build()

        val exception =
            assertThrows(UserTypeUnsupportedException::class.java) {
                converter.convert(jwt).block()
            }
        assertEquals("User type not supported", exception.message)
    }
}