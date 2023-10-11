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
package com.ritense.portal.klant.service.impl

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.commonground.authentication.JwtBuilder
import com.ritense.portal.klant.client.OpenKlantClient
import nl.nlportal.klant.generiek.client.OpenKlantClientConfig
import com.ritense.portal.klant.domain.klanten.Klant
import com.ritense.portal.klant.domain.klanten.KlantCreationRequest
import com.ritense.portal.klant.domain.klanten.KlantUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.security.oauth2.jwt.Jwt

@ExperimentalCoroutinesApi
internal class BurgerServiceTest {

    @Mock
    lateinit var openKlantClient: OpenKlantClient

    lateinit var openKlantClientConfig: OpenKlantClientConfig

    lateinit var burgerService: BurgerService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        openKlantClientConfig = OpenKlantClientConfig(rsin = "000000000")
        burgerService = BurgerService(openKlantClientConfig, openKlantClient)
    }

    @Test
    fun `getBurgerProfiel calls openklant client with BSN for burger`() = runTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        `when`(openKlantClient.getKlanten(authentication, 1, "123")).thenReturn(listOf(mock(Klant::class.java)))

        burgerService.getBurgerProfiel(authentication)
        verify(openKlantClient).getKlanten(authentication, 1, "123")
    }

    @Test
    fun `getBurgerProfiel throws exception when called with unsupported authentication`() {
        val jwt = Jwt
            .withTokenValue("token")
            .header("alg", "none")
            .claim("random", "1234")
            .build()
        val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

        val illegalArgumentException = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runTest {
                burgerService.getBurgerProfiel(authentication)
            }
        }

        assertEquals("Cannot get klant for this user", illegalArgumentException.message)
    }

    @Test
    fun `getBurgerProfiel throws exception when called with BedrijfAuthentication`() {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()

        val illegalArgumentException = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runTest {
                burgerService.getBurgerProfiel(authentication)
            }
        }

        assertEquals("Cannot get klant by KVK", illegalArgumentException.message)
    }

    @Test
    suspend fun `getBurgerProfiel throws exception when multiple klanten found`() {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        `when`(openKlantClient.getKlanten(authentication, 1, "123")).thenReturn(
            listOf(
                mock(Klant::class.java),
                mock(Klant::class.java)
            )
        )

        val illegalStateException = Assertions.assertThrows(IllegalStateException::class.java) {
            runTest {
                burgerService.getBurgerProfiel(authentication)
            }
        }

        assertEquals("Cannot get klant for this user", illegalStateException.message)
    }

    @Test
    fun `updateBurgerProfiel should update klant if exist`() = runTest {
        val klant = mock(Klant::class.java)
        `when`(klant.url).thenReturn("some-url")
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        `when`(openKlantClient.getKlanten(authentication, 1, "123")).thenReturn(listOf(klant))
        val klantUpdate = KlantUpdate("0600000000", "example@email.com")

        burgerService.updateBurgerProfiel(klantUpdate, authentication)

        verify(openKlantClient).patchKlant(eq(authentication), any(String::class.java), any(Klant::class.java))
    }

    @Test
    fun `updateBurgerProfiel should create klant if not exist`() = runTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        `when`(openKlantClient.getKlanten(authentication, 1, "123")).thenReturn(emptyList())
        val klantUpdate = KlantUpdate("0600000000", "example@email.com")

        burgerService.updateBurgerProfiel(klantUpdate, authentication)

        verify(openKlantClient).postKlant(eq(authentication), any(KlantCreationRequest::class.java))
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
    private fun <T> eq(obj: T): T = Mockito.eq<T>(obj)
}