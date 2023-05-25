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
package com.ritense.portal.zakenapi.service

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.commonground.authentication.JwtBuilder
import com.ritense.portal.zakenapi.client.ZakenApiClient
import com.ritense.portal.zakenapi.client.ZakenApiConfig
import com.ritense.portal.zakenapi.domain.ResultPage
import com.ritense.portal.zakenapi.domain.ZaakRol
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt

@ExperimentalCoroutinesApi
internal class ZakenApiServiceTest {

    var openZaakClient = mock(ZakenApiClient::class.java)
    var zaakService = ZakenApiService(openZaakClient)

    @Test
    fun `getZaken calls openzaak client with BSN for burger`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        zaakService.getZaken(5, authentication)
        verify(openZaakClient).getZaken(5, "123")
    }

    @Test
    fun `getZaken gets rollen and zaken for rollen for bedrijf`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()
        val firstZaakId = UUID.randomUUID()
        val secondZaakId = UUID.randomUUID()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/$firstZaakId"),
                    ZaakRol("http://example.com/some-path/$secondZaakId")
                )
            )
        )

        zaakService.getZaken(5, authentication)

        verify(openZaakClient).getZaakRollen(1, null, "123", null)
        verify(openZaakClient).getZaak(firstZaakId)
        verify(openZaakClient).getZaak(secondZaakId)
    }

    @Test
    fun `getZaken throws exception when called with unsupported authentication`() {
        val jwt = Jwt
            .withTokenValue("token")
            .header("alg", "none")
            .claim("random", "1234")
            .build()
        val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

        val illegalArgumentException = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlockingTest {
                zaakService.getZaken(1, authentication)
            }
        }

        assertEquals("Cannot get zaken for this user", illegalArgumentException.message)
    }

    @Test
    fun `getZaak gets zaak and checks rol for burger`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368")
                )
            )
        )

        zaakService.getZaak(uuid, authentication)

        verify(openZaakClient).getZaak(uuid)
        verify(openZaakClient).getZaakRollen(1, "123", null, uuid)
    }

    @Test
    fun `getZaak gets zaak and checks rol for bedrijf`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368")
                )
            )
        )

        zaakService.getZaak(uuid, authentication)

        verify(openZaakClient).getZaak(uuid)
        verify(openZaakClient).getZaakRollen(1, null, "123", uuid)
    }

    @Test
    fun `getZaak throws exception when getting zaak user has no rol`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        `when`(openZaakClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(1, null, null, listOf())
        )

        val illegalStateException = Assertions.assertThrows(IllegalStateException::class.java) {
            runBlockingTest {
                zaakService.getZaak(uuid, authentication)
            }
        }

        assertEquals("Access denied to this zaak", illegalStateException.message)
    }

    @Test
    fun `getZaak throws exception when called with unsupported authentication`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val jwt = Jwt
            .withTokenValue("token")
            .header("alg", "none")
            .claim("random", "1234")
            .build()
        val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

        val illegalArgumentException = Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlockingTest {
                zaakService.getZaak(uuid, authentication)
            }
        }

        assertEquals("Cannot get zaak for this user", illegalArgumentException.message)
    }

    @Test
    fun getZaakStatus() = runBlockingTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatus("http://some.domain.com/zaken/api/v1/statussen/$uuid")
        verify(openZaakClient).getStatus(uuid)
    }

    @Test
    fun getZaakStatusHistory() = runBlockingTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatusHistory(uuid)
        verify(openZaakClient).getStatusHistory(uuid)
    }
}