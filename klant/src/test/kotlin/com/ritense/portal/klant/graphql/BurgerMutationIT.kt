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
package com.ritense.portal.klant.graphql

import com.ritense.portal.commonground.authentication.WithBurgerUser
import com.ritense.portal.klant.TestHelper
import nl.nlportal.klant.generiek.client.OpenKlantClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class BurgerMutationIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val openKlantClientConfig: OpenKlantClientConfig,
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockOpenKlantServer()
        server.start()
        openKlantClientConfig.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("111111111")
    fun `updateBurgerProfiel should update klant when klant exists`() {
        val mutation = """
            mutation {
                updateBurgerProfiel(
                    klant: { telefoonnummer: "0611111111", emailadres: "updated@email.nl" }
                ) {
                    telefoonnummer
                    emailadres
                }
            }
        """.trimIndent()

        val basePath = "$.data.updateBurgerProfiel"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(mutation)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.telefoonnummer").isEqualTo("0611111111")
            .jsonPath("$basePath.emailadres").isEqualTo("updated@email.nl")
    }

    @Test
    @WithBurgerUser("222222222")
    fun `updateBurgerProfiel should create klant when klant doesn't exist`() {
        val mutation = """
            mutation {
                updateBurgerProfiel(
                    klant: { telefoonnummer: "0622222222", emailadres: "created@email.nl" }
                ) {
                    telefoonnummer
                    emailadres
                }
            }
        """.trimIndent()

        val basePath = "$.data.updateBurgerProfiel"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(mutation)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.telefoonnummer").isEqualTo("0622222222")
            .jsonPath("$basePath.emailadres").isEqualTo("created@email.nl")
    }

    fun setupMockOpenKlantServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/klanten/api/v1/klanten" -> when (request.method) {
                        "GET" -> when (Regex("Bsn=([0-9]+)").find(request.path!!)!!.groupValues[1]) {
                            "111111111" -> TestHelper.mockResponseFromFile("/data/get-klant-list-response.json")
                            "222222222" -> TestHelper.mockResponseFromFile("/data/get-empty-klant-list-response.json")
                            else -> MockResponse().setResponseCode(404)
                        }
                        "POST" -> TestHelper.mockResponseFromFile("/data/post-klant-response.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                    "/klanten/api/v1/klanten/5d479908-fbb7-49c2-98c9-9afecf8de79a" -> when (request.method) {
                        "PATCH" -> TestHelper.mockResponseFromFile("/data/put-klant-response.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }
}