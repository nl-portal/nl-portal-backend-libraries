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
class ProductTypeQueryIT(
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
            propsRegistry.add("nl-portal.config.openproduct.properties.product-type-api-url") { url }
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
    }

    @Test
    @WithBurgerUser("569312863")
    fun `get product types`() =
        runTest {
            val basePath = "$.data.getProductTypes"
            val resultPath = "$basePath.content[0]"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }
                .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getProductTypes.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.number").isEqualTo(1)
                .jsonPath("$resultPath.naam").isEqualTo("Parkeren")
                .jsonPath("$resultPath.uniformeProductNaam").isEqualTo("Parkeervergunning")
                .jsonPath("$resultPath.code").isEqualTo("PARKEREN")
                .jsonPath("$resultPath.themas[0].naam").isEqualTo("Parkeren")
                .jsonPath("$resultPath.prijzen[0].uuid").isEqualTo("317ab929-5cb4-4dde-ae4a-489f4d388699")
                .jsonPath("$resultPath.prijzen[0].prijsopties[0].bedrag").isEqualTo(100.00)
                .jsonPath(
                    "$resultPath.prijzen[0].prijsregels[0].url",
                ).isEqualTo("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen")
                .jsonPath("$resultPath.links[0].naam").isEqualTo("link naar Ritense website")
                .jsonPath("$resultPath.acties[0].naam").isEqualTo("watkanikregelen-belastingen")
                .jsonPath(
                    "$resultPath.acties[0].url",
                ).isEqualTo("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen")
                .jsonPath("$resultPath.externCodes[0].code").isEqualTo("RMWA")
                .jsonPath("$resultPath.parameters[0].naam").isEqualTo("paymentcategorie")
                .jsonPath("$resultPath.parameters[0].waarde").isEqualTo("parkeren")
                .jsonPath(
                    "$resultPath.zaaktypen[0].url",
                ).isEqualTo("http://localhost:8001/catalogi/api/v1/744ca059-f412-49d4-8963-5800e4afd486")
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get product type`() =
        runTest {
            val basePath = "$.data.getProductType"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }
                .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getProductType.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.naam").isEqualTo("Parkeren")
                .jsonPath("$basePath.uniformeProductNaam").isEqualTo("Parkeervergunning")
                .jsonPath("$basePath.code").isEqualTo("PARKEREN")
                .jsonPath("$basePath.themas[0].naam").isEqualTo("Parkeren")
                .jsonPath("$basePath.prijzen[0].uuid").isEqualTo("317ab929-5cb4-4dde-ae4a-489f4d388699")
                .jsonPath("$basePath.prijzen[0].prijsopties[0].bedrag").isEqualTo(100.00)
                .jsonPath(
                    "$basePath.prijzen[0].prijsregels[0].url",
                ).isEqualTo("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen")
                .jsonPath("$basePath.links[0].naam").isEqualTo("link naar Ritense website")
                .jsonPath("$basePath.acties[0].naam").isEqualTo("watkanikregelen-belastingen")
                .jsonPath(
                    "$basePath.acties[0].url",
                ).isEqualTo("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen")
                .jsonPath("$basePath.externCodes[0].code").isEqualTo("RMWA")
                .jsonPath("$basePath.parameters[0].naam").isEqualTo("paymentcategorie")
                .jsonPath("$basePath.parameters[0].waarde").isEqualTo("parkeren")
                .jsonPath(
                    "$basePath.zaaktypen[0].url",
                ).isEqualTo("http://localhost:8001/catalogi/api/v1/744ca059-f412-49d4-8963-5800e4afd486")
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /producttypen/dee273e9-2aa8-40ae-84b7-cb7da3c075ba/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producttype.json")
                            }
                            "GET /producttypen/" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producttypes.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}