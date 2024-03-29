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
package nl.nlportal.zakenapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.commonground.authentication.JwtBuilder
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.domain.ResultPage
import nl.nlportal.zakenapi.domain.ZaakObject
import nl.nlportal.zakenapi.domain.ZaakRol
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.server.ResponseStatusException
import java.util.*

@ExperimentalCoroutinesApi
internal class ZakenApiServiceTest {
    var documentenApiService: DocumentenApiService = mock()
    var zakenApiClient = mock(ZakenApiClient::class.java)
    var objectsApiClient: ObjectsApiClient = mock()
    var zaakService =
        ZakenApiService(
            zakenApiClient,
            documentenApiService,
            objectsApiClient,
        )

    @Test
    fun `getZaken throws exception when called with unsupported authentication`() {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "none")
                .claim("random", "1234")
                .build()
        val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

        val illegalArgumentException =
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                runTest {
                    zaakService.getZaken(1, authentication, null)
                }
            }

        assertEquals("Cannot get zaken for this user", illegalArgumentException.message)
    }

    @Test
    fun `getZaak gets zaak and checks rol for burger`() =
        runTest {
            val uuid = UUID.randomUUID()
            val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

            `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
                ResultPage(
                    1,
                    null,
                    null,
                    listOf(
                        ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368"),
                    ),
                ),
            )

            zaakService.getZaak(uuid, authentication)

            verify(zakenApiClient).getZaak(uuid)
            verify(zakenApiClient).getZaakRollen(1, "123", null, uuid)
        }

    @Test
    fun `getZaak gets zaak and checks rol for bedrijf`() =
        runTest {
            val uuid = UUID.randomUUID()
            val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()

            `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
                ResultPage(
                    1,
                    null,
                    null,
                    listOf(
                        ZaakRol("http://example.com/some-path/a5753b01-a6af-426e-96fb-9d54c7d47368"),
                    ),
                ),
            )

            zaakService.getZaak(uuid, authentication)

            verify(zakenApiClient).getZaak(uuid)
            verify(zakenApiClient).getZaakRollen(1, null, "123", uuid)
        }

    @Test
    fun `getZaak throws exception when getting zaak user has no rol`() =
        runBlocking {
            val uuid = UUID.randomUUID()
            val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()

            `when`(zakenApiClient.getZaakRollen(anyInt(), any(), any(), any())).thenReturn(
                ResultPage(1, null, null, listOf()),
            )

            val exception =
                Assertions.assertThrows(ResponseStatusException::class.java) {
                    runTest {
                        zaakService.getZaak(uuid, authentication)
                    }
                }

            assertEquals("Access denied to this zaak", exception.reason)
        }

    @Test
    fun `getZaak throws exception when called with unsupported authentication`() =
        runBlocking {
            val uuid = UUID.randomUUID()
            val jwt =
                Jwt
                    .withTokenValue("token")
                    .header("alg", "none")
                    .claim("random", "1234")
                    .build()
            val authentication = object : CommonGroundAuthentication(jwt, emptyList()) {}

            val illegalArgumentException =
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    runTest {
                        zaakService.getZaak(uuid, authentication)
                    }
                }

            assertEquals("Authentication not (yet) supported", illegalArgumentException.message)
        }

    @Test
    fun getZaakStatus() =
        runTest {
            val uuid = UUID.randomUUID()
            zaakService.getZaakStatus("http://some.domain.com/zaken/api/v1/statussen/$uuid")
            verify(zakenApiClient).getStatus(uuid)
        }

    @Test
    fun getZaakStatusHistory() =
        runTest {
            val uuid = UUID.randomUUID()
            zaakService.getZaakStatusHistory(uuid)
            verify(zakenApiClient).getStatusHistory(uuid)
        }

    @Test
    fun getZaakObjecten() =
        runTest {
            val uuid = UUID.randomUUID()
            val resultPage = ResultPage(1, null, null, listOf(mock(ZaakObject::class.java)))
            `when`(zakenApiClient.getZaakObjecten(1, uuid)).thenReturn(resultPage)
            val zaakObjecten = zaakService.getZaakObjecten(uuid)

            assertEquals(1, zaakObjecten.size)
        }
}