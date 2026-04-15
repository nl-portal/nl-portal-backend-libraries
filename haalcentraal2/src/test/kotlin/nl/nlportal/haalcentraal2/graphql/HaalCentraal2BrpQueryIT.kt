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
package nl.nlportal.haalcentraal2.graphql

import tools.jackson.databind.JsonNode
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.haalcentraal2.TestHelper
import nl.nlportal.haalcentraal2.autoconfiguration.HaalCentraal2ModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class HaalCentraal2BrpQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val haalCentraal2ModuleConfiguration: HaalCentraal2ModuleConfiguration,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        var url2: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.haalcentraal2.properties.brp-api-url") { url }
            propsRegistry.add("nl-portal.config.haalcentraal2.properties.bewoning-api-url") { url2 }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start()
            url = server?.url("/brp").toString()
            url2 = server?.url("/bewoning").toString()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            server?.shutdown()
        }
    }

    @BeforeEach
    internal fun setUp() {
        setupMockServer()
        url = server?.url("/").toString()
        url2 = server?.url("/").toString()
        haalCentraal2ModuleConfiguration.properties.brpApiUrl = url
        haalCentraal2ModuleConfiguration.properties.bewoningApiUrl = url2
    }

    @Test
    @WithBurgerUser("999993847")
    fun getPersoon() {
        val query =
            """
            query {
                getPersoonV2 {
                    burgerservicenummer,
                    naam {
                        geslachtsnaam,
                        volledigeNaam,
                        lastName,
                        officialLastName,
                    }
                    leeftijd,
                    nationaliteiten {
                        type,
                        redenOpname {
                            code,
                            omschrijving
                        },
                        nationaliteit {
                            code,
                            omschrijving
                        }
                    }
                }
            }
            """.trimIndent()

        val responseBody =
            httpGraphQlTester
                .document(query)
                .execute()
                .errors()
                .verify()
                .path("getPersoonV2")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("999993847", responseBody.get("burgerservicenummer").stringValue())
        assertEquals("Pieter Jan de Vries", responseBody.requiredAt("/naam/volledigeNaam")?.stringValue())
        assertEquals("Vries", responseBody.requiredAt("/naam/geslachtsnaam")?.stringValue())
    }

    @Test
    @WithBurgerUser("999993847")
    fun getPersoonWithBewonersAantal() {
        val query =
            """
            query {
                getPersoonV2 {
                    burgerservicenummer,
                    naam {
                        geslachtsnaam,
                        volledigeNaam,
                    },
                    bewonersAantal
                }
            }
            """.trimIndent()

        val responseBody =
            httpGraphQlTester
                .document(query)
                .execute()
                .errors()
                .verify()
                .path("getPersoonV2")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("999993847", responseBody.get("burgerservicenummer").stringValue())
        assertEquals(4, responseBody.get("bewonersAantal").intValue())
    }

    @Test
    @WithBurgerUser("999993847")
    fun getContactGegevens() {
        val query =
            """
            query {
                getPersoonV2 {
                    burgerservicenummer,
                    datumEersteInschrijvingGBA {
                        type,
                        datum,
                        langFormaat
                    },
                    geheimhoudingPersoonsgegevens,
                    naam {
                        geslachtsnaam,
                        volledigeNaam,
                    },
                    verblijfplaats {
                        adresseerbaarObjectIdentificatie,
                        verblijfadres {
                            officieleStraatnaam,
                            huisnummer,
                            huisletter,
                            huisnummertoevoeging,
                            postcode,
                            woonplaats
                        }
                    },
                    geboorte {
                        datum {
                            type,
                            datum,
                            langFormaat
                        },
                        land {
                            code,
                            omschrijving
                        },
                        plaats {
                            code,
                            omschrijving
                        }
                    },
                    nationaliteiten {
                        nationaliteit {
                            omschrijving
                        }
                    },
                    kinderen{
                        naam {
                            geslachtsnaam
                        },
                        geboorte {
                            datum {
                                type,
                                datum,
                                langFormaat
                            },
                            land {
                                code,
                                omschrijving
                            },
                            plaats {
                                code,
                                omschrijving
                            }
                        }
                    },
                    partners{
                        naam {
                            geslachtsnaam
                        }
                    },
                    ouders{
                        naam {
                            geslachtsnaam
                        }
                    }
                }
            }
            """.trimIndent()

        val responseBody =
            httpGraphQlTester
                .document(query)
                .execute()
                .errors()
                .verify()
                .path("getPersoonV2")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("999993847", responseBody.get("burgerservicenummer").stringValue())
        assertEquals(true, responseBody.get("geheimhoudingPersoonsgegevens").booleanValue())
        assertEquals("226010000038820", responseBody.requiredAt("/verblijfplaats/adresseerbaarObjectIdentificatie")?.stringValue())
        assertEquals("Het Spui 1", responseBody.requiredAt("/verblijfplaats/verblijfadres/officieleStraatnaam")?.stringValue())
        assertEquals("Nederlands", responseBody.requiredAt("/nationaliteiten/0/nationaliteit/omschrijving")?.stringValue())
    }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "POST /brp/personen" -> {
                                TestHelper.mockResponseFromFile("/data/get-personen.json")
                            }

                            "POST /bewoning/bewoningen" -> {
                                TestHelper.mockResponseFromFile("/data/get-bewoningen.json")
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}