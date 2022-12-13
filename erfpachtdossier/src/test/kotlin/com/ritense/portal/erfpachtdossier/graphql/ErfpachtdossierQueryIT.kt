/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.erfpachtdossier.graphql

import com.ritense.portal.commonground.authentication.WithBurgerUser
import com.ritense.portal.erfpachtdossier.TestHelper
import com.ritense.portal.erfpachtdossier.client.ErfpachtDossierClientConfig
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
import java.time.Duration

@SpringBootTest
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ErfpachtdossierQueryIT(
    @Autowired private var testClient: WebTestClient,
    @Autowired private val dossierClientConfig: ErfpachtDossierClientConfig
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockDossierServer()
        server.start()
        dossierClientConfig.url = server.url("/").toString()

        testClient = testClient.mutate()
            .responseTimeout(Duration.ofMillis(60000)).build()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("123")
    fun getDossier() {
        val query = """
            query {
                getDossier(123) {
                    dossierNummer
                }
            }
        """.trimIndent()

        val basePath = "$.data"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
    }

    @Test
    @WithBurgerUser("123")
    fun getDossiers() {
        val query = """
            query {
                getDossiers {
                    dossiers {
                        dossierNummer,
                        kadaster,
                        eersteUitgifte
                    }
                }
            }
        """.trimIndent()

        val basePath = "$.data.getDossiers.dossiers"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath(basePath).exists()
            .jsonPath("$basePath").isArray
            .jsonPath("$basePath[0].dossierNummer").isEqualTo("E1001/01")
            .jsonPath("$basePath[1].dossierNummer").isEqualTo("E1000/01")
            .jsonPath("$basePath[0].kadaster").isArray
            .jsonPath("$basePath[0].kadaster[0]").isEqualTo("ASD02/SB/990/G/4")
            .jsonPath("$basePath[0].kadaster[1]").isEqualTo("ASD02/SB/999/G/5")
            .jsonPath("$basePath[1].kadaster").doesNotExist()
            .jsonPath("$basePath[0].eersteUitgifte").isEqualTo("2020-12-12")
            .jsonPath("$basePath[1].eersteUitgifte").isEqualTo("2019-12-12")
    }

    fun setupMockDossierServer() {
        val dispatcher: Dispatcher = object: Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path?.substringBefore('?')
                val response = when(path) {
                    "/api/erfpachtdossiers" ->
                        TestHelper.mockResponseFromFile("/data/erfpachtdossiers_from_vernise.json")
                    "/api/erfpachtdossier/123" ->
                        TestHelper.mockResponseFromFile("/data/erfpachtdossier_from_vernise.json")
                    else -> MockResponse().setResponseCode(404)
                }

                return response
            }
        }
        server.dispatcher = dispatcher
    }
}