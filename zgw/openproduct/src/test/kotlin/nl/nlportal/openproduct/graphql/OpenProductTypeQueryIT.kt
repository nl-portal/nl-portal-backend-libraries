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

import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openproduct.TestHelper
import nl.nlportal.openproduct.TestHelper.readFileAsString
import nl.nlportal.openproduct.autoconfigure.OpenProductModuleConfiguration
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
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenProductTypeQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
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
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProductTypes.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProductTypes")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals(1, responseBody.get("number")?.intValue())
            assertEquals("Parkeren", responseBody.requiredAt("/content/0/naam")?.textValue())
            assertEquals("Parkeervergunning", responseBody.requiredAt("/content/0/uniformeProductNaam")?.textValue())
            assertEquals("PARKEREN", responseBody.requiredAt("/content/0/code")?.textValue())
            assertEquals("317ab929-5cb4-4dde-ae4a-489f4d388699", responseBody.requiredAt("/content/0/prijzen/0/uuid")?.textValue())
            assertEquals(100.00, responseBody.requiredAt("/content/0/prijzen/0/prijsopties/0/bedrag")?.doubleValue())
            assertEquals("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen", responseBody.requiredAt("/content/0/prijzen/0/prijsregels/0/url")?.textValue())
            assertEquals("link naar Ritense website", responseBody.requiredAt("/content/0/links/0/naam")?.textValue())
            assertEquals("watkanikregelen-belastingen", responseBody.requiredAt("/content/0/acties/0/naam")?.textValue())
            assertEquals("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen", responseBody.requiredAt("/content/0/acties/0/url")?.textValue())
            assertEquals("RMWA", responseBody.requiredAt("/content/0/externCodes/0/code")?.textValue())
            assertEquals("paymentcategorie", responseBody.requiredAt("/content/0/parameters/0/naam")?.textValue())
            assertEquals("parkeren", responseBody.requiredAt("/content/0/parameters/0/waarde")?.textValue())
            assertEquals("http://localhost:8001/catalogi/api/v1/744ca059-f412-49d4-8963-5800e4afd486", responseBody.requiredAt("/content/0/zaaktypen/0/url")?.textValue())
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get product type`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProductType.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProductType")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals("Parkeren", responseBody.requiredAt("/naam")?.textValue())
            assertEquals("Parkeervergunning", responseBody.requiredAt("/uniformeProductNaam")?.textValue())
            assertEquals("PARKEREN", responseBody.requiredAt("/code")?.textValue())
            assertEquals("317ab929-5cb4-4dde-ae4a-489f4d388699", responseBody.requiredAt("/prijzen/0/uuid")?.textValue())
            assertEquals(100.00, responseBody.requiredAt("/prijzen/0/prijsopties/0/bedrag")?.doubleValue())
            assertEquals("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen", responseBody.requiredAt("/prijzen/0/prijsregels/0/url")?.textValue())
            assertEquals("link naar Ritense website", responseBody.requiredAt("/links/0/naam")?.textValue())
            assertEquals("watkanikregelen-belastingen", responseBody.requiredAt("/acties/0/naam")?.textValue())
            assertEquals("http://localhost:9000/engine-rest/decision-definition/key/alg-belastingen", responseBody.requiredAt("/acties/0/url")?.textValue())
            assertEquals("RMWA", responseBody.requiredAt("/externCodes/0/code")?.textValue())
            assertEquals("paymentcategorie", responseBody.requiredAt("/parameters/0/naam")?.textValue())
            assertEquals("parkeren", responseBody.requiredAt("/parameters/0/waarde")?.textValue())
            assertEquals("http://localhost:8001/catalogi/api/v1/744ca059-f412-49d4-8963-5800e4afd486", responseBody.requiredAt("/zaaktypen/0/url")?.textValue())
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /producttypen/dee273e9-2aa8-40ae-84b7-cb7da3c075ba/content" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producttype-content.json")
                            }
                            "GET /producttypen/dee273e9-2aa8-40ae-84b7-cb7da3c075ba" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-producttype.json")
                            }
                            "GET /producttypen" -> {
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