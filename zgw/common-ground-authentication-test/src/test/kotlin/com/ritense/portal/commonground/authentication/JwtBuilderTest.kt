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
package com.ritense.portal.commonground.authentication

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class JwtBuilderTest {

    @Test
    fun getBurgerAuthentication() {
        val authentication = JwtBuilder().aanvragerBsn("1234").buildBurgerAuthentication()
        assertEquals("1234", authentication.getBsn())
    }

    @Test
    fun getBurgerGemachtigdeAuthentication() {
        val authentication = JwtBuilder()
            .aanvragerBsn("1234")
            .gemachtigdeBsn("5678")
            .buildBurgerAuthentication()
        assertEquals("1234", authentication.getBsn())
        assertEquals("5678", authentication.getGemachtigde()?.bsn)
        assertNull(authentication.getGemachtigde()?.kvk)
    }

    @Test
    fun getBurgerJwt() {
        val jwt = JwtBuilder().aanvragerBsn("1234").buildJwt()
        val aanvrager = jwt.getClaim<Map<String, Any>>(AANVRAGER_KEY)
        assertEquals("1234", aanvrager[BSN_KEY])
    }

    @Test
    fun `getBurgerGemachtigdeJwt with gemachtigde bsn`() {
        val jwt = JwtBuilder()
            .aanvragerBsn("1234")
            .gemachtigdeBsn("5678")
            .buildJwt()

        val aanvrager = jwt.getClaim<Map<String, Any>>(AANVRAGER_KEY)
        val gemachtigde = jwt.getClaim<Map<String, Any>>(GEMACHTIGDE_KEY)
        assertEquals("1234", aanvrager[BSN_KEY])
        assertEquals("5678", gemachtigde[BSN_KEY])
    }

    @Test
    fun getBedrijfAuthentication() {
        val authentication = JwtBuilder().aanvragerKvk("1234").buildBedrijfAuthentication()
        assertEquals("1234", authentication.getKvkNummer())
    }

    @Test
    fun getBedrijfJwt() {
        val jwt = JwtBuilder().aanvragerKvk("1234").buildJwt()
        val aanvrager = jwt.getClaim<Map<String, Any>>(AANVRAGER_KEY)
        assertEquals("1234", aanvrager[KVK_NUMMER_KEY])
    }

    @Test
    fun `buildJwt throws exception when no bsn or kvk has been set`() {
        assertThrows<IllegalStateException> { JwtBuilder().buildJwt() }
    }

    @Test
    fun `buildBedrijfAuthentication throws exception when no bsn or kvk has been set`() {
        assertThrows<IllegalStateException> { JwtBuilder().buildBedrijfAuthentication() }
    }

    @Test
    fun `buildBurgerAuthentication throws exception when no bsn or kvk has been set`() {
        assertThrows<IllegalStateException> { JwtBuilder().buildBurgerAuthentication() }
    }

    @Test
    fun `aanvragerKvk throws exception when bsn has already been set`() {
        assertThrows<AssertionError> {
            JwtBuilder()
                .aanvragerBsn("123")
                .aanvragerKvk("456")
        }
    }

    @Test
    fun `aanvragerBsn throws exception when kvk has already been set`() {
        assertThrows<AssertionError> {
            JwtBuilder()
                .aanvragerKvk("456")
                .aanvragerBsn("123")
        }
    }

    @Test
    fun `buildJwt return jwt with only latest gemachtigde entry`() {
        val jwt = JwtBuilder()
            .aanvragerBsn("123")
            .gemachtigdeBsn("123")
            .gemachtigdeKvk("123")
            .buildJwt()

        val gemachtigde = jwt.getClaim<Map<String, Any>>(GEMACHTIGDE_KEY)
        assertNull(gemachtigde[BSN_KEY])
        assertEquals("123", gemachtigde[KVK_NUMMER_KEY])
    }

    @Test
    fun `buildBedrijfAuthentication throws exception when aanvrager kvk has not been set`() {
        assertThrows<IllegalStateException> {
            JwtBuilder()
                .aanvragerBsn("123")
                .buildBedrijfAuthentication()
        }
    }

    @Test
    fun `buildBurgerAuthentication throws exception when aanvrager bsn has not been set`() {
        assertThrows<IllegalStateException> {
            JwtBuilder()
                .aanvragerKvk("123")
                .buildBurgerAuthentication()
        }
    }
}