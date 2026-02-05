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

import tools.jackson.databind.JsonNode
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.product.TestHelper
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ProductMutationIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val graphqlUpdateProductVerbruiksObject: String,
) {
    lateinit var server: MockWebServer
    lateinit var url: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockOpenZaakServer()
        server.start()
        url = server.url("/").toString()
        objectsApiClientConfig.properties.url = server.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("")
    fun updateVerbruikdObjectTestUnauthorized() {
        val basePath = "$.data.updateProductVerbruiksObject[0]"

        httpGraphQlTester
            .document(graphqlUpdateProductVerbruiksObject)
            .execute()
            .errors()
    }

    @Test
    @WithBurgerUser("569312863")
    fun updateVerbruikdObjectTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlUpdateProductVerbruiksObject)
                .execute()
                .errors()
                .verify()
                .path("updateProductVerbruiksObject")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("2d725c07-2f26-4705-8637-438a42b5a800", responseBody.requiredAt("/id")?.stringValue())
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

                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5a800" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-object.json")
                            }

                            "PUT /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5a800" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-object.json")
                            }

                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5ac2d" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product.json")
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }
}