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

import java.net.URI
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.openproduct.TestHelper
import nl.nlportal.openproduct.TestHelper.readFileAsString
import nl.nlportal.openproduct.autoconfigure.OpenProductModuleConfiguration
import nl.nlportal.zakenapi.client.ZakenApiConfig
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import tools.jackson.databind.JsonNode

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenProductQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val openProductModuleConfiguration: OpenProductModuleConfiguration,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val zakenApiConfig: ZakenApiConfig,
    @Autowired private val documentApisConfig: DocumentApisConfig,
) {
    companion object {
        private const val KNOWN_DOC_ID = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"

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
            server?.start(9000)
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
        documentApisConfig.properties.getConfig("openzaak").url = server?.url("/").toString()
    }

    @Test
    @WithBurgerUser("569312863")
    fun `get producten`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProducten.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProducten")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals(4, responseBody.get("totalElements")?.intValue())
            assertEquals("http://localhost:8070/producten/api/v1/producten/694242af-d906-470b-b7e1-eb3527886854/", responseBody.requiredAt("/content/0/url")?.stringValue())
            assertEquals("2025-04-30", responseBody.requiredAt("/content/0/startDatum")?.stringValue())
            assertEquals("PARKEREN", responseBody.requiredAt("/content/0/producttype/code")?.stringValue())
            assertEquals(30, responseBody.requiredAt("/content/0/verbruiksobject/uren")?.intValue())
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get product`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProduct.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProduct")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals("http://localhost:8070/producten/api/v1/producten/694242af-d906-470b-b7e1-eb3527886854/", responseBody.requiredAt("/url")?.stringValue())
            assertEquals("2025-04-30", responseBody.requiredAt("/startDatum")?.stringValue())
            assertEquals("PARKEREN", responseBody.requiredAt("/producttype/code")?.stringValue())
            assertEquals(30, responseBody.requiredAt("/verbruiksobject/uren")?.intValue())
            assertEquals("Lopende zaak", responseBody.requiredAt("/zaken/0/omschrijving")?.stringValue())
            assertEquals("Very important task", responseBody.requiredAt("/taken/0/titel")?.stringValue())
        }

    @Test
    @WithBurgerUser("569312864")
    fun `get producten by thema id`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProductenByThema.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProductenByThema")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals(5, responseBody.size())
            assertEquals("http://localhost:8070/producten/api/v1/producten/694242af-d906-470b-b7e1-eb3527886854/", responseBody.requiredAt("/0/url")?.stringValue())
            assertEquals("2025-04-30", responseBody.requiredAt("/0/startDatum")?.stringValue())
            assertEquals("PARKEREN", responseBody.requiredAt("/0/producttype/code")?.stringValue())
            assertEquals(30, responseBody.requiredAt("/0/verbruiksobject/uren")?.intValue())
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

                            "POST /engine-rest/decision-definition/key/alg-belastingen/evaluate" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-dmn-decision.json")
                            }

                            "GET /acties" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-acties.json")
                            }

                            "GET /enkelvoudiginformatieobjecten/${KNOWN_DOC_ID}" -> {
                                TestHelper.mockResponse(TestHelper.handleDocumentResponse)
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}