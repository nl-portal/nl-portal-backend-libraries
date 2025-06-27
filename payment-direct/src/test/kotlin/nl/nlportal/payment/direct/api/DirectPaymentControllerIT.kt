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
package nl.nlportal.payment.direct.api

import nl.nlportal.payment.direct.TestHelper
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import nl.nlportal.payment.direct.service.DirectPaymentService
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.nio.charset.Charset
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DirectPaymentControllerIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val directPaymentModuleConfiguration: DirectPaymentModuleConfiguration,
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockObjectsApiServer()
        server.start()
        objectsApiClientConfig.properties.url = server.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun postSaleTest() {
        val body =
            """
            {
              "apiVersion": "v1",
              "created": "2020-12-09T11:20:40.3744722+01:00",
              "id": "34b8a607-1fce-4003-b3ae-a4d29e92b232",
              "merchantId": "TAX",
              "payment": {
                "paymentOutput": {
                  "amountOfMoney": {
                    "amount": 1000,
                    "currencyCode": "EUR"
                  },
                  "references": {
                    "merchantReference": "58fad5ab-dc2f-11ec-9075-f22a405ce707"
                  },
                  "paymentMethod": "card"
                },
                "status": "COMPLETED",
                "statusOutput": {
                  "isCancellable": false,
                  "statusCategory": "CREATED",
                  "statusCode": 9,
                  "isAuthorized": false,
                  "isRefundable": false
                },
                "id": "***3092546156***"
              },
              "type": "payment.created"
            }

            """.trimIndent()

        val signature =
            createSignature(
                body = body,
                secretKey =
                    directPaymentModuleConfiguration.properties.configurations["belastingzaken"]
                        ?.webhookApiSecret
                        .toString(),
            )
        val headers = HttpHeaders()
        headers.add(DirectPaymentService.HEADER_X_GCS_SIGNATURE, signature)
        headers.add(
            DirectPaymentService.HEADER_X_GCS_KEYID,
            directPaymentModuleConfiguration.properties.configurations["belastingzaken"]?.webhookApiKey,
        )
        webTestClient
            .post()
            .uri(PAYMENT_DIRECT_POSTSALE_ENDPOINT)
            .headers {
                it.addAll(headers)
            }.contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("Request Successful processed")
    }

    @Test
    fun postSaleTestOrderIdNotUUID() {
        val body =
            """
            {
              "apiVersion": "v1",
              "created": "2020-12-09T11:20:40.3744722+01:00",
              "id": "34b8a607-1fce-4003-b3ae-a4d29e92b232",
              "merchantId": "TAX",
              "payment": {
                "paymentOutput": {
                  "amountOfMoney": {
                    "amount": 1000,
                    "currencyCode": "EUR"
                  },
                  "references": {
                    "merchantReference": "58fad5ab"
                  },
                  "paymentMethod": "card"
                },
                "status": "COMPLETED",
                "statusOutput": {
                  "isCancellable": false,
                  "statusCategory": "CREATED",
                  "statusCode": 9,
                  "isAuthorized": false,
                  "isRefundable": false
                },
                "id": "***3092546156***"
              },
              "type": "payment.created"
            }

            """.trimIndent()

        val signature =
            createSignature(
                body = body,
                secretKey =
                    directPaymentModuleConfiguration.properties.configurations["belastingzaken"]
                        ?.webhookApiSecret
                        .toString(),
            )
        val headers = HttpHeaders()
        headers.add(DirectPaymentService.HEADER_X_GCS_SIGNATURE, signature)
        headers.add(
            DirectPaymentService.HEADER_X_GCS_KEYID,
            directPaymentModuleConfiguration.properties.configurations["belastingzaken"]?.webhookApiKey,
        )
        webTestClient
            .post()
            .uri(PAYMENT_DIRECT_POSTSALE_ENDPOINT)
            .headers {
                it.addAll(headers)
            }.contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("Request Successful processed")
    }

    @Test
    fun postSaleTestInvalid() {
        val body =
            """
            {
              "apiVersion": "v1",
              "created": "2020-12-09T11:20:40.3744722+01:00",
              "id": "34b8a607-1fce-4003-b3ae-a4d29e92b232",
              "merchantId": "TAX",
              "payment": {
                "paymentOutput": {
                  "amountOfMoney": {
                    "amount": 1000,
                    "currencyCode": "EUR"
                  },
                  "references": {
                    "merchantReference": "58fad5ab-dc2f-11ec-9075-f22a405ce707"
                  },
                  "paymentMethod": "card"
                },
                "status": "COMPLETED",
                "statusOutput": {
                  "isCancellable": false,
                  "statusCategory": "CREATED",
                  "statusCode": 9,
                  "isAuthorized": false,
                  "isRefundable": false
                },
                "id": "***3092546156***"
              },
              "type": "payment.created"
            }

            """.trimIndent()

        val signature =
            directPaymentModuleConfiguration.properties.configurations["belastingzaken"]
                ?.webhookApiSecret
                .toString()
        val headers = HttpHeaders()
        headers.add(DirectPaymentService.HEADER_X_GCS_SIGNATURE, signature)
        headers.add(
            DirectPaymentService.HEADER_X_GCS_KEYID,
            directPaymentModuleConfiguration.properties.configurations["belastingzaken"]?.webhookApiKey,
        )
        webTestClient
            .post()
            .uri(PAYMENT_DIRECT_POSTSALE_ENDPOINT)
            .headers {
                it.addAll(headers)
            }.contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("Request is not valid")
    }

    @Test
    fun postSaleTestIncorrectOgoneStatus() {
        val body =
            """
            {
              "apiVersion": "v1",
              "created": "2020-12-09T11:20:40.3744722+01:00",
              "id": "34b8a607-1fce-4003-b3ae-a4d29e92b232",
              "merchantId": "TAX",
              "payment": {
                "paymentOutput": {
                  "amountOfMoney": {
                    "amount": 1000,
                    "currencyCode": "EUR"
                  },
                  "references": {
                    "merchantReference": "58fad5ab-dc2f-11ec-9075-f22a405ce707"
                  },
                  "paymentMethod": "card"
                },
                "status": "COMPLETED",
                "statusOutput": {
                  "isCancellable": false,
                  "statusCategory": "CREATED",
                  "statusCode": 50,
                  "isAuthorized": false,
                  "isRefundable": false
                },
                "id": "***3092546156***"
              },
              "type": "payment.created"
            }

            """.trimIndent()

        val signature =
            createSignature(
                body = body,
                secretKey =
                    directPaymentModuleConfiguration.properties.configurations["belastingzaken"]
                        ?.webhookApiSecret
                        .toString(),
            )
        val headers = HttpHeaders()
        headers.add(DirectPaymentService.HEADER_X_GCS_SIGNATURE, signature)
        headers.add(
            DirectPaymentService.HEADER_X_GCS_KEYID,
            directPaymentModuleConfiguration.properties.configurations["belastingzaken"]?.webhookApiKey,
        )
        webTestClient
            .post()
            .uri(PAYMENT_DIRECT_POSTSALE_ENDPOINT)
            .headers {
                it.addAll(headers)
            }.contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBody
            .contentToString()
            .contentEquals("200 OK \"Request has not the correct status\"")
    }

    fun setupMockObjectsApiServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "GET /api/v2/objects/58fad5ab-dc2f-11ec-9075-f22a405ce707" -> {
                                TestHelper.mockResponseFromFile("/data/get-task.json")
                            }
                            "PUT /api/v2/objects/58fad5ab-dc2f-11ec-9075-f22a405ce707" -> {
                                TestHelper.mockResponseFromFile("/data/put-task.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }

    companion object {
        const val PAYMENT_DIRECT_POSTSALE_ENDPOINT: String = "/api/public/payment/direct/postsale"

        fun createSignature(
            body: String,
            secretKey: String,
        ): String {
            val hmac = Mac.getInstance("HmacSHA256")
            val key = SecretKeySpec(secretKey.toByteArray(charset = Charset.forName("UTF-8")), "HmacSHA256")
            hmac.init(key)

            val unencodedResult = hmac.doFinal(body.toByteArray(charset = Charset.forName("UTF-8")))
            return Base64.encodeBase64String(unencodedResult)
        }
    }
}