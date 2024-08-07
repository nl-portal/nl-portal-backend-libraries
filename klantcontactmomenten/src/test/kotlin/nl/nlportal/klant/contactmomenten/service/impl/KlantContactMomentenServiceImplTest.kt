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
package nl.nlportal.klant.contactmomenten.service.impl

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.commonground.authentication.JwtBuilder
import nl.nlportal.klant.client.OpenKlantClient
import nl.nlportal.klant.domain.klanten.Klant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.klant.contactmomenten.client.KlantContactMomentenClient
import nl.nlportal.klant.contactmomenten.domain.ContactMoment
import nl.nlportal.klant.generiek.domain.ResultPage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
internal class KlantContactMomentenServiceImplTest {
    @Mock
    lateinit var klantContactMomentenClient: KlantContactMomentenClient

    @Mock
    lateinit var klantClient: OpenKlantClient

    lateinit var klantContactMomentenServiceImpl: KlantContactMomentenServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        klantContactMomentenServiceImpl = KlantContactMomentenServiceImpl(klantContactMomentenClient, klantClient)
    }

    @Test
    fun `get klantcontactmomenten with BSN for burger`() =
        runTest {
            val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
            val klant = mock(Klant::class.java)
            `when`(klant.url).thenReturn("http://dummy.nl")
            `when`(klantClient.getKlanten(authentication, 1, "123")).thenReturn(listOf(klant))
            `when`(klantContactMomentenClient.getKlantContactMomenten(authentication, "http://dummy.nl", 1)).thenReturn(
                ResultPage(
                    1,
                    null,
                    null,
                    listOf(
                        mock(ContactMoment::class.java),
                    ),
                ),
            )

            val result = klantContactMomentenServiceImpl.getKlantContactMomenten(authentication, 1)
            assertEquals(1, result?.content?.size)
        }

    @Test
    fun `get klantcontactmomenten with BSN for burger get meerdere klanten`() {
        val illegalStateException =
            Assertions.assertThrows(IllegalStateException::class.java) {
                runTest {
                    val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
                    val klant = mock(Klant::class.java)
                    `when`(klant.url).thenReturn("http://dummy.nl")
                    `when`(klantClient.getKlanten(authentication, 1, "123")).thenReturn(listOf(klant, klant))
                    `when`(
                        klantContactMomentenClient.getKlantContactMomenten(
                            authentication,
                            "http://dummy.nl",
                            1,
                        ),
                    ).thenReturn(
                        ResultPage(
                            1,
                            null,
                            null,
                            listOf(
                                mock(ContactMoment::class.java),
                            ),
                        ),
                    )
                    klantContactMomentenServiceImpl.getKlantContactMomenten(authentication, 1)
                }
            }
        assertEquals("Multiple klanten found for BSN: 123", illegalStateException.message)
    }

    @Test
    fun `get klantcontactmomenten with BSN for burger maar geen klanten gevonden`() =
        runTest {
            val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
            `when`(klantClient.getKlanten(authentication, 1, "123")).thenReturn(listOf())
            `when`(klantContactMomentenClient.getKlantContactMomenten(authentication, "http://dummy.nl", 1)).thenReturn(
                ResultPage(
                    1,
                    null,
                    null,
                    listOf(
                        mock(ContactMoment::class.java),
                    ),
                ),
            )

            val result = klantContactMomentenServiceImpl.getKlantContactMomenten(authentication, 1)
            assertNull(result)
        }

    @Test
    fun `get klantcontactmomenten  with BedrijfAuthentication`() {
        val authentication = JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication()
        val illegalArgumentException =
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                runTest {
                    klantContactMomentenServiceImpl.getKlantContactMomenten(authentication, 1)
                }
            }
        assertEquals("Cannot get klant by KVK", illegalArgumentException.message)
    }

    @Test
    fun `get klantcontactmomenten  with unknown Authentication`() {
        val authentication = mock(CommonGroundAuthentication::class.java)
        val illegalArgumentException =
            Assertions.assertThrows(IllegalArgumentException::class.java) {
                runTest {
                    klantContactMomentenServiceImpl.getKlantContactMomenten(authentication, 1)
                }
            }
        assertEquals("Cannot get klant for this user", illegalArgumentException.message)
    }

    @Test
    fun `get objectcontactmomenten`() =
        runTest {
            val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
            `when`(
                klantContactMomentenClient.getObjectContactMomenten(
                    authentication,
                    "http://dummy.nl",
                    1,
                ),
            ).thenReturn(
                ResultPage(
                    1,
                    null,
                    null,
                    listOf(
                        mock(ContactMoment::class.java),
                    ),
                ),
            )

            val result = klantContactMomentenServiceImpl.getObjectContactMomenten(authentication, "http://dummy.nl", 1)
            assertEquals(1, result.content.size)
        }
}