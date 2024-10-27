/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.graphql

import nl.nlportal.berichten.TestHelper
import nl.nlportal.berichten.TestHelper.verifyOnlyDataExists
import nl.nlportal.berichten.autoconfigure.BerichtenConfigurationProperties
import nl.nlportal.berichten.service.BerichtenService
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BerichtenQueryIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val berichtenConfigurationProperties: BerichtenConfigurationProperties,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
) {
    @SpyBean lateinit var berichtenService: BerichtenService

    lateinit var mockObjectenApi: MockWebServer

    @BeforeEach
    fun setUp() {
        mockObjectenApi = MockWebServer()
        setupMockObjectsApiServer()
        mockObjectenApi.start()
        objectsApiClientConfig.url = mockObjectenApi.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        mockObjectenApi.shutdown()
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return BerichtenPage`() {
        val basePath = "$.data.getBerichten"
        webTestClient
            .post()
            .uri { builder ->
                builder
                    .path("/graphql")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
            .body(BodyInserters.fromValue(TestHelper.graphqlBerichtenPageRequest))
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.content[0].berichtType").isEqualTo("NOTIFICATIE")
            .jsonPath("$basePath.content[0].geopend").isEqualTo("false")
            .jsonPath("$basePath.content[0].publicatiedatum").isEqualTo("2024-07-18")
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should update bericht before return`() {
        val basePath = "$.data.getBericht"
        webTestClient
            .post()
            .uri { builder ->
                builder
                    .path("/graphql")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
            .body(BodyInserters.fromValue(TestHelper.graphqlValidBerichtRequest))
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.berichtType").isEqualTo("NOTIFICATIE")
            .jsonPath("$basePath.geopend").isEqualTo("true")
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should return bericht`() {
        val basePath = "$.data.getBericht"
        webTestClient
            .post()
            .uri { builder ->
                builder
                    .path("/graphql")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
            .body(BodyInserters.fromValue(TestHelper.graphqlValidBerichtReadRequest))
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.berichtType").isEqualTo("NOTIFICATIE")
            .jsonPath("$basePath.geopend").isEqualTo("true")
    }

    @WithBurgerUser("111111110")
    @Test
    fun `should return null for bericht request that doesn't belong to user`() {
        val basePath = "$.data.getBericht"
        webTestClient
            .post()
            .uri { builder ->
                builder
                    .path("/graphql")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
            .body(BodyInserters.fromValue(TestHelper.graphqlValidBerichtReadRequest))
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should return null instead of exception for invalid bericht request`() {
        val basePath = "$.data.getBericht"
        webTestClient
            .post()
            .uri { builder ->
                builder
                    .path("/graphql")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
            .body(BodyInserters.fromValue(TestHelper.graphqlInvalidBerichtRequest))
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should return unopened berichten`() {
        val basePath = "$.data.getUnopenedBerichtenCount"
        webTestClient
            .post()
            .uri { builder ->
                builder
                    .path("/graphql")
                    .build()
            }
            .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
            .body(BodyInserters.fromValue(TestHelper.graphqlUnopenedBerichtenCountRequest))
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath(basePath).isEqualTo(2)
    }

    fun setupMockObjectsApiServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                    val response =
                        when (request.method + " " + path) {
                            "GET /api/v2/objects" -> {
                                if (queryParams.any { it.contains("identificatie__value__exact__999990755") }) {
                                    TestHelper.mockResponse(TestHelper.objectenApiBerichtenPageResponse)
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            } // a4961c4a-29a7-4cc7-9d5d-bceed1dfccba
                            "GET /api/v2/objects/9e021130-8cbd-4c6f-846a-677448e21ce8" -> {
                                TestHelper.mockResponse(TestHelper.objectenApiBerichtObjectResponse)
                            }
                            "GET /api/v2/objects/a4961c4a-29a7-4cc7-9d5d-bceed1dfccba" -> {
                                TestHelper.mockResponse(TestHelper.objectenApiBerichtIsReadObjectResponse)
                            }
                            "PUT /api/v2/objects/9e021130-8cbd-4c6f-846a-677448e21ce8" -> {
                                TestHelper.mockResponse(TestHelper.objectenApiBerichtIsReadObjectResponse)
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        mockObjectenApi.dispatcher = dispatcher
    }
}