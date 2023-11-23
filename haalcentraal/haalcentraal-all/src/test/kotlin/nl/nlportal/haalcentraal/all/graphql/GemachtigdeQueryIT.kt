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
package nl.nlportal.haalcentraal.all.graphql

import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.haalcentraal.all.TestHelper
import nl.nlportal.haalcentraal.client.HaalCentraalClientConfig
import nl.nlportal.haalcentraal.hr.client.HaalCentraalHrClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GemachtigdeQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val haalCentraalClientConfig: HaalCentraalClientConfig,
    @Autowired private val haalCentraalHrClientConfig: HaalCentraalHrClientConfig,
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockServer()
        server.start()

        haalCentraalClientConfig.url = server.url("/").toString()
        haalCentraalHrClientConfig.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("318634776", gemachtigdeBsn = "999993847")
    fun `getGemachtigde with bsn`() {
        val query = """
            query {
                getGemachtigde {
                    persoon {
                        aanhef,
                        voorletters,
                        voornamen,
                        voorvoegsel,
                        geslachtsnaam
                    },
                    bedrijf {
                        naam
                    }
                }
            }
        """.trimIndent()

        val basePath = "$.data.getGemachtigde"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.persoon.aanhef").isEqualTo("Geachte mevrouw Kooyman")
            .jsonPath("$basePath.persoon.voorletters").isEqualTo("M.")
            .jsonPath("$basePath.persoon.voornamen").isEqualTo("Merel")
            .jsonPath("$basePath.persoon.voorvoegsel").isEqualTo("de")
            .jsonPath("$basePath.persoon.geslachtsnaam").isEqualTo("Kooyman")
            .jsonPath("$basePath.bedrijf.naam").doesNotExist()
    }

    @Test
    @WithBurgerUser("318634776", gemachtigdeKvk = "90012768")
    fun `getGemachtigde with kvk`() {
        val query = """
            query {
                getGemachtigde {
                    persoon {
                        aanhef,
                        voorletters,
                        voornamen,
                        voorvoegsel,
                        geslachtsnaam
                    },
                    bedrijf {
                        naam
                    }
                }
            }
        """.trimIndent()

        val basePath = "$.data.getGemachtigde"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.bedrijf.naam").isEqualTo("Test bedrijf")
            .jsonPath("$basePath.persoon.aanhef").doesNotExist()
    }

    private fun setupMockServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/brp/ingeschrevenpersonen/999993847" -> TestHelper.mockResponseFromFile("/data/get-ingeschreven-persoon.json")
                    "/basisprofielen/90012768" -> TestHelper.mockResponseFromFile("/data/get-maatschappelijke-activiteiten.json")
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }
}