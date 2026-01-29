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
package nl.nlportal.payment.direct.graphql

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.payment.direct.TestHelper
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class DirectPaymentQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val directPaymentModuleConfiguration: DirectPaymentModuleConfiguration,
) {
    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.payment.direct.properties.url") { url }
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
        directPaymentModuleConfiguration.properties.url = url
    }

    @Test
    @WithBurgerUser("123")
    fun getDirectPaymentStatus() {
        val query =
            """
            query {
                getDirectPaymentStatus(
                    identifier: "belastingzaken", 
                    hostedCheckoutId: "4373041363"
                ) {
                status,
                }
            }
            """.trimIndent()

        val responseBody =
            httpGraphQlTester
                .document(query)
                .execute()
                .errors()
                .verify()
                .path("getDirectPaymentStatus")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("SUCCESSFUL", responseBody.get("status")?.textValue())
    }

    fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /v2/TAX/hostedcheckouts/4373041363" -> {
                                TestHelper.mockResponseFromFile("/data/payment-status-response.json")
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