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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

internal class KeycloakAuthenticationTest {
    @Test
    fun `getBsn returns bsn from JWT claims`() {
        val jwt = JwtBuilder().aanvragerUid(UUID.fromString("12341234-1234-1234-1234-123412341234")).buildJwt()

        val authentication = KeycloakUserAuthentication(jwt, emptyList())

        assertEquals("12341234-1234", authentication.getUid())
    }

    @Test
    fun `getBsn returns bsn from JWT claims when using gemachtigde claim`() {
        val jwt = JwtBuilder().aanvragerUid(UUID.fromString("12341234-1234-1234-1234-123412341234")).gemachtigdeBsn("5678").buildJwt()

        val authentication = KeycloakUserAuthentication(jwt, emptyList())

        assertEquals("12341234-1234", authentication.getUid())
        assertEquals("5678", authentication.getGemachtigde()?.bsn)
        assertNull(authentication.getGemachtigde()?.kvk)
    }
}