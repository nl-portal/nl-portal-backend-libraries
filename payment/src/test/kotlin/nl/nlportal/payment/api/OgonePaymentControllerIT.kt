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
package nl.nlportal.payment.api

import nl.nlportal.payment.TestHelper
import nl.nlportal.payment.autoconfiguration.OgonePaymentConfig
import nl.nlportal.payment.domain.OgonePayment
import nl.nlportal.payment.domain.PaymentField
import nl.nlportal.payment.service.OgonePaymentService
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
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
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OgonePaymentControllerIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val paymentConfig: OgonePaymentConfig,
) {
    lateinit var server: MockWebServer

    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockObjectsApiServer()
        server.start()
        objectsApiClientConfig.url = server.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun postSaleTest() {
        val parameterList =
            listOf(
                PaymentField(OgonePayment.PAYMENT_PROPERTY_ORDER_ID, "58fad5ab-dc2f-11ec-9075-f22a405ce707"),
                PaymentField(OgonePayment.PAYMENT_PROPERTY_STATUS, "91"),
            )

        val shaSign =
            OgonePaymentService.hashParameters(
                parameterList,
                paymentConfig.getPaymentProfile("belastingzaken")!!.shaOutKey,
                paymentConfig.getPaymentProfile("belastingzaken")!!.shaVersion,
            )

        webTestClient.get()
            .uri("/api/payment/ogone/postsale?ORDERID=58fad5ab-dc2f-11ec-9075-f22a405ce707&STATUS=91&SHASIGN=$shaSign")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("Request Successful processed")
    }

    @Test
    fun postSaleTestInvalid() {
        val parameterList =
            listOf(
                PaymentField(OgonePayment.PAYMENT_PROPERTY_ORDER_ID, "58fad5ab-dc2f-11ec-9075-f22a405ce707"),
                PaymentField(OgonePayment.PAYMENT_PROPERTY_STATUS, "91"),
            )

        val shaSign =
            OgonePaymentService.hashParameters(
                parameterList,
                paymentConfig.getPaymentProfile("belastingzaken")!!.shaOutKey,
                paymentConfig.getPaymentProfile("belastingzaken")!!.shaVersion,
            )

        webTestClient.get()
            .uri("/api/payment/ogone/postsale?ORDERID=58fad5ab-dc2f-11ec-9075-f22a405ce707&STATUS=91&AMOUNT=200&SHASIGN=$shaSign")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("Request is not valid")
    }

    @Test
    fun postSaleTestNotFromPaymentProvider() {
        webTestClient.get()
            .uri("/api/payment/ogone/postsale?orderID=58fad5ab-dc2f-11ec-9075-f22a405ce707&PSPID=TAX&STATUS=91")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("400 BAD_REQUEST \"Request is not from payment provider\"")
    }

    @Test
    fun postSaleTestIncorrectOgoneStatus() {
        webTestClient.get()
            .uri("/api/payment/ogone/postsale?orderID=58fad5ab-dc2f-11ec-9075-f22a405ce707&PSPID=TAX&STATUS=1")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("400 BAD_REQUEST \"Request has not the correct status\"")
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
                                if (queryParams.any { it.contains("verwerker_taak_id__exact__58fad5ab-dc2f-11ec-9075-f22a405ce707") }) {
                                    TestHelper.mockResponseFromFile("/data/get-task.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }
                            "PUT /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5ac2d" -> {
                                TestHelper.mockResponseFromFile("/data/put-task.json")
                            }

                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }
}