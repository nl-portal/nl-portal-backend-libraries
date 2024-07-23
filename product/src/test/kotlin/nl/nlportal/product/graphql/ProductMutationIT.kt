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
package nl.nlportal.product.graphql

import nl.nlportal.product.TestHelper
import nl.nlportal.product.TestHelper.verifyOnlyDataExists
import nl.nlportal.product.client.ProductConfig
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
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
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ProductMutationIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val productApiConfig: ProductConfig,
    @Autowired private val taakObjectConfig: TaakObjectConfig,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val graphqlUpdateProductVerbruiksObject: String,
    @Autowired private val graphqlPrefill: String,
) {
    lateinit var server: MockWebServer
    lateinit var url: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockOpenZaakServer()
        server.start()
        url = server.url("/").toString()
        objectsApiClientConfig.url = server.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("")
    fun updateVerbruikdObjectTestUnauthorized() {
        val basePath = "$.data.updateProductVerbruiksObject[0]"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlUpdateProductVerbruiksObject)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @Test
    @WithBurgerUser("569312864")
    fun updateVerbruikdObjectTestNotFound() {
        val basePath = "$.data.updateProductVerbruiksObject"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlUpdateProductVerbruiksObject)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @Test
    @WithBurgerUser("569312863")
    fun updateVerbruikdObjectTestBurger() {
        val basePath = "$.data.updateProductVerbruiksObject"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlUpdateProductVerbruiksObject)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.id").isEqualTo("2d725c07-2f26-4705-8637-438a42b5a800")
    }

    @Test
    @WithBurgerUser("569312863")
    fun prefillTestBurger() {
        val basePath = "$.data.prefill"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlPrefill)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.objectId").isEqualTo("f9d7f166-bcea-4448-a984-4e717e558458")
            .jsonPath("$basePath.formulierUrl").isEqualTo("http://localhost:8080/formuliernaam")
    }

    fun setupMockOpenZaakServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                    val response =
                        when (request.method + " " + path) {
                            "GET /api/v2/objects" -> {
                                if (queryParams.any { it.contains("naam__exact__erfpacht") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-types.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }
                            "POST /api/v2/objects" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-prefill-object.json")
                            }
                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5a800" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-object.json")
                            }
                            "PUT /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5a800" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-object.json")
                            }
                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5ac2d" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }
}