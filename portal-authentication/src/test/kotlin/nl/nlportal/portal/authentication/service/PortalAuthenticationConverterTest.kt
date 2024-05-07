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
package nl.nlportal.portal.authentication.service

import nl.nlportal.portal.authentication.JwtBuilder
import nl.nlportal.portal.authentication.exception.UserTypeUnsupportedException
import nl.nlportal.portal.authentication.domain.PortalAuthentication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertTrue

class PortalAuthenticationConverterTest {
    lateinit var converter: PortalAuthenticationConverter

    @BeforeEach
    fun setUp() {
        converter = PortalAuthenticationConverter()
    }

    @Test
    fun `have authenticated token`() {
        val uuid = UUID.randomUUID()
        val jwt = JwtBuilder().addKeycloakUUIDToClaims(uuid).build()
        val authentication = converter.convert(jwt)

        assertTrue(authentication.block() is PortalAuthentication)
    }

    @Test
    fun `converter throws when there is no uuid`() {
        val jwt = JwtBuilder().buildJwt()

        val exception =
            assertThrows(UserTypeUnsupportedException::class.java) {
                converter.convert(jwt).block()
            }
        assertEquals("User type not supported", exception.message)
    }
}