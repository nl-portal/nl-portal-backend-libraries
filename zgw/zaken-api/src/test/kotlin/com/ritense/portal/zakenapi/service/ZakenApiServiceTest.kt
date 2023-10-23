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

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.commonground.authentication.JwtBuilder
import com.ritense.portal.documentenapi.service.DocumentenApiService
import com.ritense.portal.zakenapi.client.ZakenApiClient
import com.ritense.portal.zakenapi.client.ZakenApiConfig
import com.ritense.portal.zakenapi.domain.ResultPage
import com.ritense.portal.zakenapi.domain.ZaakObject
import com.ritense.portal.zakenapi.domain.ZaakRol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@ExperimentalCoroutinesApi
internal class ZakenApiServiceTest {

    var documentenApiService: DocumentenApiService = mock()
    var zakenApiClient = mock(ZakenApiClient::class.java)
    var objectsApiClient: ObjectsApiClient = mock()
    var zakenApiConfig: ZakenApiConfig = mock()
    var objectMapper: ObjectMapper = mock()
    var zaakService = ZakenApiService(
        zakenApiClient,
        documentenApiService,
        objectsApiClient,
        objectMapper
    )

    @Test
    fun `getZaken calls openzaak client with BSN for burger`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        zaakService.getZaken(5, authentication)
        verify(zakenApiClient).getZaken(5, "123")
    }

    @Test
    fun `getZaken gets rollen and zaken for rollen for bedrijf`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()
        val firstZaakId = UUID.randomUUID()
        val secondZaakId = UUID.randomUUID()

        `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/$firstZaakId"),
                    ZaakRol("http://example.com/some-path/$secondZaakId")
                )
            )
        )

        zaakService.getZaken(5, authentication)

        verify(zakenApiClient).getZaakRollen(1, null, "123", null)
        verify(zakenApiClient).getZaak(firstZaakId)
        verify(zakenApiClient).getZaak(secondZaakId)
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

        `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368")
                )
            )
        )

        zaakService.getZaak(uuid, authentication)

        verify(zakenApiClient).getZaak(uuid)
        verify(zakenApiClient).getZaakRollen(1, "123", null, uuid)
    }

    @Test
    fun `getZaak gets zaak and checks rol for bedrijf`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()

        `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(
                1, null, null,
                listOf(
                    ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368")
                )
            )
        )

        zaakService.getZaak(uuid, authentication)

        verify(zakenApiClient).getZaak(uuid)
        verify(zakenApiClient).getZaakRollen(1, null, "123", uuid)
    }

    @Test
    fun `getZaak throws exception when getting zaak user has no rol`() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

        `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
            ResultPage(1, null, null, listOf())
        )

        val exception = Assertions.assertThrows(ResponseStatusException::class.java) {
            runBlockingTest {
                zaakService.getZaak(uuid, authentication)
            }
        }

        assertEquals("Access denied to this zaak", exception.reason)
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

        assertEquals("Authentication not (yet) supported", illegalArgumentException.message)
    }

    @Test
    fun getZaakStatus() = runBlockingTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatus("http://some.domain.com/zaken/api/v1/statussen/$uuid")
        verify(zakenApiClient).getStatus(uuid)
    }

    @Test
    fun getZaakStatusHistory() = runBlockingTest {
        val uuid = UUID.randomUUID()
        zaakService.getZaakStatusHistory(uuid)
        verify(zakenApiClient).getStatusHistory(uuid)
    }

    @Test
    fun getZaakObjecten() = runBlockingTest {
        val uuid = UUID.randomUUID()
        val resultPage = ResultPage(1, null, null, listOf(mock(ZaakObject::class.java)))
        `when`(zakenApiClient.getZaakObjecten(1, uuid)).thenReturn(resultPage)
        val zaakObjecten = zaakService.getZaakObjecten(uuid)

        assertEquals(1, zaakObjecten.size)
    }
}