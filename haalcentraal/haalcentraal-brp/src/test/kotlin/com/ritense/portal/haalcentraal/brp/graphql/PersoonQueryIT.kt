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
package com.ritense.portal.haalcentraal.brp.graphql

import com.ritense.portal.commonground.authentication.WithBurgerUser
import com.ritense.portal.haalcentraal.brp.TestHelper
import com.ritense.portal.haalcentraal.client.HaalCentraalClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.contains
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PersoonQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val haalCentraalClientConfig: HaalCentraalClientConfig
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockServer()
        server.start()

        haalCentraalClientConfig.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("999993847")
    fun getNaam() {
        val query = """
            query {
                getPersoon {
                    naam {
                        aanhef,
                        voorletters,
                        voornamen,
                        voorvoegsel,
                        geslachtsnaam
                    }
                }
            }
        """.trimIndent()

        val basePath = "$.data.getPersoon"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.naam.aanhef").isEqualTo("Geachte mevrouw Kooyman")
            .jsonPath("$basePath.naam.voorletters").isEqualTo("M.")
            .jsonPath("$basePath.naam.voornamen").isEqualTo("Merel")
            .jsonPath("$basePath.naam.voorvoegsel").isEqualTo("de")
            .jsonPath("$basePath.naam.geslachtsnaam").isEqualTo("Kooyman")
    }

    @Test
    @WithBurgerUser("999993847")
    fun getContactGegevens() {
        val query = """
            query {
                getPersoon {
                    burgerservicenummer,
                    geslachtsaanduiding,
                    naam {
                        voornamen,
                        geslachtsnaam
                    },
                    verblijfplaats {
                        straat,
                        huisnummer,
                        huisletter,
                        huisnummertoevoeging,
                        postcode,
                        woonplaats
                    },
                    geboorte {
                        datum {
                            datum,
                            jaar,
                            maand,
                            dag
                        },
                        land {
                            omschrijving
                        }
                    },
                    nationaliteiten {
                        nationaliteit {
                            omschrijving
                        }
                    }
                }
            }
        """.trimIndent()

        val basePath = "$.data.getPersoon"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.burgerservicenummer").isEqualTo("999993847")
            .jsonPath("$basePath.geslachtsaanduiding").isEqualTo("vrouw")
            .jsonPath("$basePath.naam.voornamen").isEqualTo("Merel")
            .jsonPath("$basePath.naam.geslachtsnaam").isEqualTo("Kooyman")
            .jsonPath("$basePath.geboorte.datum.datum").isEqualTo("1982-04-10")
            .jsonPath("$basePath.geboorte.datum.jaar").isEqualTo(1982)
            .jsonPath("$basePath.geboorte.datum.maand").isEqualTo(4)
            .jsonPath("$basePath.geboorte.datum.dag").isEqualTo(10)
            .jsonPath("$basePath.geboorte.land.omschrijving").isEqualTo("Nederland")
            .jsonPath("$basePath.verblijfplaats.straat").isEqualTo("Hector Berliozplantsoen")
            .jsonPath("$basePath.verblijfplaats.huisnummer").isEqualTo("31")
            .jsonPath("$basePath.verblijfplaats.huisletter").isEqualTo("H")
            .jsonPath("$basePath.verblijfplaats.huisnummertoevoeging").isEqualTo("Achter")
            .jsonPath("$basePath.verblijfplaats.postcode").isEqualTo("2551XS")
            .jsonPath("$basePath.verblijfplaats.woonplaats").isEqualTo("'s-Gravenhage")
            .jsonPath("$basePath.nationaliteiten").isArray()
            .jsonPath("$basePath.nationaliteiten[*].nationaliteit.omschrijving", contains("Nederlandse", "Franse"))
    }

    private fun setupMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/brp/ingeschrevenpersonen/999993847" -> TestHelper.mockResponseFromFile("/data/get-ingeschreven-persoon.json")
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }
}