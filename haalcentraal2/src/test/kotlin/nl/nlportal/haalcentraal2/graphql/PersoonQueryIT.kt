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

import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.haalcentraal2.TestHelper
import nl.nlportal.haalcentraal2.TestHelper.verifyOnlyDataExists
import nl.nlportal.haalcentraal2.autoconfiguration.HaalCentraal2ModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class PersoonQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val haalCentraal2ModuleConfiguration: HaalCentraal2ModuleConfiguration,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.haalcentraal2.properties.url") { url }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start()
            url = server?.url("/").toString()
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
        haalCentraal2ModuleConfiguration.properties.url = url
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
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getPersoonV2"

        testClient
            .post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.burgerservicenummer")
            .isEqualTo("999993847")
            .jsonPath("$basePath.naam.volledigeNaam")
            .isEqualTo("Pieter Jan de Vries")
            .jsonPath("$basePath.naam.geslachtsnaam")
            .isEqualTo("Vries")
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

        val basePath = "$.data.getPersoonV2"

        testClient
            .post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.burgerservicenummer")
            .isEqualTo("999993847")
            .jsonPath("$basePath.bewonersAantal")
            .isEqualTo(4)
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

        val basePath = "$.data.getPersoonV2"

        testClient
            .post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath(basePath)
            .exists()
            .jsonPath("$basePath.burgerservicenummer")
            .isEqualTo("999993847")
            .jsonPath("$basePath.geheimhoudingPersoonsgegevens")
            .isEqualTo(true)
            .jsonPath("$basePath.nationaliteiten")
            .isArray()
            .jsonPath("$basePath.nationaliteiten[0].nationaliteit.omschrijving")
            .isEqualTo("Nederlands")
            .jsonPath("$basePath.verblijfplaats.verblijfadres.officieleStraatnaam")
            .isEqualTo("Het Spui 1")
            .jsonPath("$basePath.verblijfplaats.adresseerbaarObjectIdentificatie")
            .isEqualTo("226010000038820")
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
                            "POST /bewoning/bewoningen" -> TestHelper.mockResponseFromFile("/data/get-bewoningen.json")
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}