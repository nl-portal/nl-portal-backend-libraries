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

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.payment.direct.TestHelper
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.function.Consumer

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class DirectPaymentMutationIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val directPaymentModuleConfiguration: DirectPaymentModuleConfiguration,
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockObjectsApiServer()
        server.start()
        directPaymentModuleConfiguration.properties.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("123")
    fun doDirectPaymentWithIdentifier() {
        val mutation =
            """
            mutation {
                doDirectPayment(
                    paymentRequest: { 
                        identifier: "belastingzaken", 
                        amount: 100.25, 
                        orderId: "123456", 
                        reference: "12345",
                    }
                ) {
                redirectUrl,
                }
            }
            """.trimIndent()

        val basePath = "$.data.doDirectPayment"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(mutation)
            .exchange()
            .expectBody()
            .consumeWith(Consumer { t -> logger.info { t } })
            .jsonPath(basePath).exists()
            .jsonPath(
                "$basePath.redirectUrl",
            ).isEqualTo(
                "https://payment.preprod.direct.worldline-solutions.com/hostedcheckout/PaymentMethods/Selection/e61340e579e04172a740676ffdda162e",
            )
    }

    @Test
    @WithBurgerUser("123")
    fun doDirectPaymentWithInvalidIdentifier() {
        val mutation =
            """
            mutation {
                doDirectPayment(
                    paymentRequest: { 
                        identifier: "invalid-identifier", 
                        amount: 100.25, 
                        orderId: "123456", 
                        reference: "12345",
                    }
                ) {
                redirectUrl,
                }
            }
            """.trimIndent()

        val basePath = "$.errors"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(mutation)
            .exchange()
            .expectBody()
            .consumeWith(Consumer { t -> logger.info { t } })
            .jsonPath(basePath).exists()
            .jsonPath(
                "$basePath[0].message",
            ).isEqualTo(
                "Exception while fetching data (/doDirectPayment) : 400 BAD_REQUEST \"Could not found direct payment profile for the identifier DirectPaymentRequest(identifier=invalid-identifier, amount=100.25, reference=12345, orderId=123456, langId=null, returnUrl=null).identifier\"",
            )
    }

    fun setupMockObjectsApiServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "POST /v2/TAX/hostedcheckouts" -> {
                                TestHelper.mockResponseFromFile("/data/payment-response.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}