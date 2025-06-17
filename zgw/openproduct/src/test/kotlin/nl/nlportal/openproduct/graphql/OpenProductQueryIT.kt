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
import nl.nlportal.zakenapi.client.ZakenApiConfig
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
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
class OpenProductQueryIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val openProductModuleConfiguration: OpenProductModuleConfiguration,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val zakenApiConfig: ZakenApiConfig,
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
            propsRegistry.add("nl-portal.config.openproduct.properties.product-type-api-url") { url }
            propsRegistry.add("nl-portal.config.zakenapi.properties.url") { url }
            propsRegistry.add("nl-portal.config.objectenapi.properties.url") { url }
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
        openProductModuleConfiguration.properties.productTypeApiUrl = URI(url)
        objectsApiClientConfig.properties.url = URI(url)
        zakenApiConfig.properties.url = url
    }

    @Test
    @WithBurgerUser("569312863")
    fun `get producten`() =
        runTest {
            val basePath = "$.data.getOpenProducten"
            val resultPath = "$basePath.content[0]"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProducten.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.numberOfElements")
                .isEqualTo(4)
                .jsonPath(
                    "$resultPath.url",
                ).isEqualTo("http://localhost:8070/producten/api/v1/producten/694242af-d906-470b-b7e1-eb3527886854/")
                .jsonPath("$resultPath.startDatum")
                .isEqualTo("2025-04-30")
                .jsonPath("$resultPath.producttype.code")
                .isEqualTo("PARKEREN")
                .jsonPath("$resultPath.verbruiksobject.uren")
                .isEqualTo(30)
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get product is allowed`() =
        runTest {
            val basePath = "$.data.getOpenProduct"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProduct.gql")))
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
                .jsonPath("$basePath.zaken[0].omschrijving")
                .isEqualTo("Lopende zaak")
                .jsonPath("$basePath.taken[0].titel")
                .isEqualTo("Very important task")
        }

    @Test
    @WithBurgerUser("569312864")
    fun `get product is not allowed`() =
        runTest {
            val basePath = "$.data.getOpenProduct"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProduct.gql")))
                .exchange()
                .expectBody()
                .jsonPath(
                    "errors[0].message",
                ).isEqualTo("Exception while fetching data (/getOpenProduct) : 401 UNAUTHORIZED \"Not authorized\"")
        }

    @Test
    @WithBurgerUser("569312864")
    fun `get producten by thema id`() =
        runTest {
            val basePath = "$.data.getOpenProductenByThema"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductenByThema.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath(
                    "$basePath[0].url",
                ).isEqualTo("http://localhost:8070/producten/api/v1/producten/694242af-d906-470b-b7e1-eb3527886854/")
                .jsonPath("$basePath[0].startDatum")
                .isEqualTo("2025-04-30")
                .jsonPath("$basePath[0].producttype.code")
                .isEqualTo("PARKEREN")
                .jsonPath("$basePath[0].verbruiksobject.uren")
                .isEqualTo(30)
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /producten/694242af-d906-470b-b7e1-eb3527886854" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-product.json")
                            }
                            "GET /themas/41f71c2e-9e0c-4a1b-8d39-709669b256c2" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-thema.json")
                            }
                            "GET /producten" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producten.json")
                            }
                            "GET /producttypen/dee273e9-2aa8-40ae-84b7-cb7da3c075ba" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producttype.json")
                            }
                            "GET /api/v2/objects/4b5f4fba-0746-11ed-b939-0242ac120023" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-taak.json")
                            }
                            "GET /zaken/api/v1/zaken/703af290-abe0-418c-b9c3-10a65e662788" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-zaak.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}