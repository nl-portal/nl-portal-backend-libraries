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
class OpenProductThemaQueryIT(
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
        openProductModuleConfiguration.properties.productTypeApiUrl = URI(url)
        objectsApiClientConfig.properties.url = URI(url)
        zakenApiConfig.properties.url = url
    }

    @Test
    @WithBurgerUser("569312863")
    fun `get themas`() =
        runTest {
            val basePath = "$.data.getOpenProductThemas"
            val resultPath = "$basePath.content[0]"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThemas.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.totalElements")
                .isEqualTo(4)
                .jsonPath("$resultPath.naam")
                .isEqualTo("Parkeren")
                .jsonPath("$resultPath.producttypen[0].uniformeProductNaam")
                .isEqualTo("Parkeervergunning")
                .jsonPath("$resultPath.producttypen[0].code")
                .isEqualTo("PARKEREN")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get hoofd themas`() =
        runTest {
            val basePath = "$.data.getOpenProductHoofdThemas"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductHoofdThemas.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.size()")
                .isEqualTo(2)
                .jsonPath("$basePath[0].naam")
                .isEqualTo("Belastingzaken")
                .jsonPath("$basePath[0].producttypen[0].uniformeProductNaam")
                .isEqualTo("toeristenbelasting")
                .jsonPath("$basePath[0].producttypen[0].code")
                .isEqualTo("BELASTINGZAKEN")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get themas hierarchy`() =
        runTest {
            val basePath = "$.data.getOpenProductThemasHierarchy"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThemasHierarchy.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.size()")
                .isEqualTo(2)
                .jsonPath("$basePath[0].thema.naam")
                .isEqualTo("Belastingzaken")
                .jsonPath("$basePath[0].thema.producttypen[0].uniformeProductNaam")
                .isEqualTo("toeristenbelasting")
                .jsonPath("$basePath[0].thema.producttypen[0].code")
                .isEqualTo("BELASTINGZAKEN")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get themas zaken`() =
        runTest {
            val basePath = "$.data.getOpenProductThemaZaken"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThemaZaken.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.size()")
                .isEqualTo(1)
                .jsonPath("$basePath[0].omschrijving")
                .isEqualTo("Lopende zaak")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get themas taken`() =
        runTest {
            val basePath = "$.data.getOpenProductThemaTaken"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThemaTaken.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.size()")
                .isEqualTo(1)
                .jsonPath("$basePath[0].id")
                .isEqualTo("2d725c07-2f26-4705-8637-438a42b5ac2d")
                .jsonPath("$basePath[0].titel")
                .isEqualTo("Taak linked to Zaak")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get thema`() =
        runTest {
            val basePath = "$.data.getOpenProductThema"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThema.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.naam")
                .isEqualTo("Parkeren")
                .jsonPath("$basePath.producttypen[0].uniformeProductNaam")
                .isEqualTo("Parkeervergunning")
                .jsonPath("$basePath.producttypen[0].code")
                .isEqualTo("PARKEREN")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get thema hierarchy`() =
        runTest {
            val basePath = "$.data.getOpenProductThemaHierarchy"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }.header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThemaHierarchy.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.size()")
                .isEqualTo(1)
                .jsonPath("$basePath[0].thema.naam")
                .isEqualTo("HoofdThema")
                .jsonPath("$basePath[0].subThemas[0].thema.naam")
                .isEqualTo("Sub thema")
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                    val response =
                        when (request.method + " " + path) {
                            "GET /themas/41f71c2e-9e0c-4a1b-8d39-709669b256c2/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-thema.json")
                            }
                            "GET /themas/5422b0e4-18ae-4017-bcea-fb03446a8136/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-subthema.json")
                            }
                            "GET /themas/c111ce6f-308f-4e6d-9460-ffe6a07e283a/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-hoofdthema.json")
                            }
                            "GET /themas/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-themas.json")
                            }
                            "POST /zaken/api/v1/zaken/_zoek" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-zaken.json")
                            }
                            "GET /producttypen/dee273e9-2aa8-40ae-84b7-cb7da3c075ba/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producttype.json")
                            }
                            "GET /api/v2/objects" -> {
                                if (queryParams.any { it.contains("identificatie__value__exact__569312863") }
                                ) {
                                    TestHelper.mockResponseFromFile("/config/data/get-taken.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}