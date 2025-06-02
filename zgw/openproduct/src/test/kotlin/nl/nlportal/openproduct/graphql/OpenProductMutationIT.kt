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
package nl.nlportal.openproduct.graphql

import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openproduct.TestHelper
import nl.nlportal.openproduct.TestHelper.verifyOnlyDataExists
import nl.nlportal.openproduct.autoconfigure.OpenProductModuleConfiguration
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
import java.net.URI

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenProductMutationIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val openProductModuleConfiguration: OpenProductModuleConfiguration,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.openproduct.properties.product-api-url") { url }
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
        openProductModuleConfiguration.properties.productApiUrl = URI(url)
    }

    @Test
    @WithBurgerUser("569312863")
    fun `update product is allowed`() =
        runTest {
            val basePath = "$.data.updateProduct"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/updateProduct.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath(
                    "$basePath.url",
                ).isEqualTo("http://localhost:8070/producten/api/v1/producten/694242af-d906-470b-b7e1-eb3527886854/")
                .jsonPath("$basePath.startDatum")
                .isEqualTo("2025-04-30")
                .jsonPath("$basePath.producttype.code")
                .isEqualTo("PARKEREN")
                .jsonPath("$basePath.verbruiksobject.uren")
                .isEqualTo(30)
        }

    @Test
    @WithBurgerUser("569312864")
    fun `get product is not allowed`() =
        runTest {
            val basePath = "$.data.updateProduct"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/updateProduct.gql")))
                .exchange()
                .expectBody()
                .jsonPath(
                    "errors[0].message",
                ).isEqualTo("Exception while fetching data (/updateProduct) : 401 UNAUTHORIZED \"Not authorized\"")
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /producten/694242af-d906-470b-b7e1-eb3527886854/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-product.json")
                            }
                            "PATCH /producten/694242af-d906-470b-b7e1-eb3527886854/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-product.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}