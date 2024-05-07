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
import nl.nlportal.zakenapi.client.ZakenApiConfig
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
import java.net.URI
import java.util.*

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ProductQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val productApiConfig: ProductConfig,
    @Autowired private val taakObjectConfig: TaakObjectConfig,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val zakenApiConfig: ZakenApiConfig,
    @Autowired private val graphqlGetProduct: String,
    @Autowired private val graphqlGetProducten: String,
    @Autowired private val graphqlGetProductZaken: String,
    @Autowired private val graphqlGetProductTaken: String,
    @Autowired private val graphqlGetProductZakenNotFound: String,
    @Autowired private val graphqlGetProductVerbruiksObjecten: String,
    @Autowired private val graphqlGetProductType: String,
) {
    lateinit var server: MockWebServer
    lateinit var url: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockOpenZaakServer()
        server.start()
        url = server.url("/").toString()
        objectsApiClientConfig.url = URI(url)
        objectsApiClientConfig.url = server.url("/").toUri()
        zakenApiConfig.url = url
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("")
    fun getProductenTestUnauthorized() {
        val basePath = "$.data.getProducten[0]"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProducten)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @Test
    @WithBurgerUser("569312864")
    fun getProductenTestNotFound() {
        val basePath = "$.data.getProducten"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProducten)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductenTestBurger() {
        val basePath = "$.data.getProducten"
        val resultPath = "$basePath.content[0]"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProducten)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$resultPath.naam").isEqualTo("erfpacht")
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTestBurger() {
        val basePath = "$.data.getProduct"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProduct)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.naam").isEqualTo("erfpacht")
            .jsonPath("$basePath.zaken[0].uuid").isEqualTo("7d9cd6c2-8147-46f2-9ae9-c67e8213c202")
            .jsonPath("$basePath.zaken[0].omschrijving").isEqualTo("Lopende zaak")
            .jsonPath("$basePath.taken[0].title").isEqualTo("Very important task")
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductZakenTestBurger() {
        val basePath = "$.data.getProductZaken"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProductZaken)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath[0].omschrijving").isEqualTo("Lopende zaak")
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductZakenTestBurgerNotFound() {
        val basePath = "$.data.getProductZaken"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProductZakenNotFound)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductVerbruiksObjectenTestBurger() {
        val basePath = "$.data.getProductVerbruiksObjecten"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProductVerbruiksObjecten)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath[0].type").isEqualTo("test verbruiksobject")
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTypeTestBurger() {
        val basePath = "$.data.getProductType"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProductType)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.naam").isEqualTo("erfpacht")
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTakenTestBurger() {
        val basePath = "$.data.getProductTaken"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProductTaken)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.size()").isEqualTo(2)
            .jsonPath("$basePath[0].id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath("$basePath[0].title").isEqualTo("Taak linked to Zaak")
    }

    @Test
    @WithBurgerUser("569312864")
    fun getProductTakenTestBurgerNoTaken() {
        val basePath = "$.data.getProductTaken"

        testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetProductTaken)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.size()").isEqualTo(0)
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
                                if (queryParams.any { it.contains("identificatie__value__exact__569312863") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-taken.json")
                                } else if (queryParams.any { it.contains("identificatie__value__exact__569312864") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-taken-empty.json")
                                } else if (queryParams.any {
                                        it.contains(
                                            "productInstantie__exact__2d725c07-2f26-4705-8637-438a42b5ac2d",
                                        )
                                    }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-details.json")
                                } else if (queryParams.any { it.contains("rollen__initiator__identificatie__exact__569312863") } &&
                                    queryParams.any { it.contains("PDCProductType__exact__7d9cd6c2-8147-46f2-9ae9-c67e8213c200") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-producten.json")
                                } else if (queryParams.any { it.contains("naam__exact__erfpacht") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-types.json")
                                } else if (queryParams.any { it.contains("rollen__initiator__identificatie__exact__569312863") } &&
                                    queryParams.any { it.contains("productInstantie__exact__7d9cd6c2-8147-46f2-9ae9-c67e8213c500") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-objecten.json")
                                } else if (queryParams.any { it.contains("rollen__initiator__identificatie__exact__569312863") } &&
                                    queryParams.any { it.contains("id__exact__2d725c07-2f26-4705-8637-438a42b5ac2d") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-producten.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }
                            "GET /zaken/api/v1/zaken" -> {
                                if (queryParams.any { it.contains("zaaktype") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-zaken.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }
                            "GET /api/v2/objects/7d9cd6c2-8147-46f2-9ae9-c67e8213c116" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-object.json")
                            }
                            "GET /api/v2/objects/7d9cd6c2-8147-46f2-9ae9-c67e8213c200" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-type.json")
                            }
                            "GET /zaken/api/v1/zaken/7d9cd6c2-8147-46f2-9ae9-c67e8213c202" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-zaak.json")
                            }
                            "GET /api/v2/objects/58fad5ab-dc2f-11ec-9075-f22a405ce708" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-taak.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }
}