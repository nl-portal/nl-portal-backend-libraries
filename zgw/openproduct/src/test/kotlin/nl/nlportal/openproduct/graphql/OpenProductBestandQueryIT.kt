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

import tools.jackson.databind.JsonNode
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openproduct.TestHelper
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
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.URI
import nl.nlportal.openproduct.TestHelper.readFileAsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenProductBestandQueryIT(
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
        openProductModuleConfiguration.properties.productTypeApiUrl = URI(url)
    }

    @Test
    @WithBurgerUser("569312863")
    fun `get bestanden`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProductBestanden.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProductBestanden")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals(1, responseBody.get("number")?.intValue())
            assertEquals("http://localhost:8070/media/https%3A/gemeente.open-product.nl/media/test.txt", responseBody.requiredAt("/content/0/bestand")?.textValue())
        }

    @Test
    @WithBurgerUser("569312863")
    fun `get actie`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(readFileAsString("/config/graphql/getOpenProductBestand.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getOpenProductBestand")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals("http://localhost:8070/media/https%3A/gemeente.open-product.nl/media/test.txt", responseBody.requiredAt("/bestand")?.textValue())
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /bestanden/0a9ff804-d151-477b-81aa-09e16f3064d9" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-bestand.json")
                            }
                            "GET /bestanden" -> {
                                TestHelper.mockResponseFromFile("/config/data/get-bestanden.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}