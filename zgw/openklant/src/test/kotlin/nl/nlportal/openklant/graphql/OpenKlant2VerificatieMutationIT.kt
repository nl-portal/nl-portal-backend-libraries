/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.graphql

import java.net.URI
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.TestHelper.verifyOnlyDataExists
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
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
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2VerificatieMutationIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val openKlantModuleConfiguration: OpenKlantModuleConfiguration,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.openklant2.properties.verificatie.url") { url }
            propsRegistry.add("nl-portal.config.openklant2.properties.klantinteractiesApiUrl") { url }
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
        openKlantModuleConfiguration.properties.verificatie.url = url
        openKlantModuleConfiguration.properties.klantinteractiesApiUrl = URI(url)
    }

    @Test
    @WithBurgerUser("569312863")
    fun `create verificatie`() =
        runTest {
            val basePath = "$.data.createVerificatie"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/createVerificatie.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.success")
                .isEqualTo(true)
        }

    @Test
    @WithBurgerUser("569312863")
    fun `verify verificatie`() =
        runTest {
            val basePath = "$.data.verifyVerificatie"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/verifyVerificatie.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.verified")
                .isEqualTo(true)
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "POST /api/verification-requests" -> {
                                TestHelper.mockResponseFromFile("/config/data/create-verificatie-response.json")
                            }
                            "POST /api/verification-requests/verify" -> {
                                TestHelper.mockResponseFromFile("/config/data/verify-verificatie-response.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}