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
package nl.nlportal.payment.qrlink.api

import java.nio.charset.Charset
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.ShaVersion
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import nl.nlportal.payment.qrlink.TestHelper
import nl.nlportal.payment.qrlink.autoconfiguratie.QRLinkPaymentModuleConfiguration
import nl.nlportal.payment.qrlink.filter.QRLinkPaymentAuthorizationFilter
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class QRLinkPaymentControllerIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val qrLinkPaymentModuleConfiguration: QRLinkPaymentModuleConfiguration,
    @Autowired private val directPaymentModuleConfiguration: DirectPaymentModuleConfiguration,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var path: String = "/api/public/payment/qrlink"

        @JvmStatic
        var url: String = ""

        @JvmStatic
        var secret: String = ""

        @JvmStatic
        var identifier = ""

        @JvmStatic
        var reference = ""

        @JvmStatic
        var orderId = ""

        @JvmStatic
        var amountInCents = 0

        @JvmStatic
        var amount = 0.00

        @JvmStatic
        var hash = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.payment.direct.properties.url") { url }
            propsRegistry.add("nl-portal.config.payment.qrlink.properties.secret") { secret }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start()
            url = server?.url("/").toString()
            secret = "123gdgg122"
            identifier = "belastingzaken"
            reference = "56789"
            orderId = "123456"
            amountInCents = 10024
            amount = amountInCents.toDouble() / 100
            val concatenateProperties = secret + identifier + amount.toString() + orderId + reference
            hash = CoreUtils.createHash(concatenateProperties, ShaVersion.SHA1.version)
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
        directPaymentModuleConfiguration.properties.url = url
        qrLinkPaymentModuleConfiguration.properties.secret = secret
    }

    @Test
    fun `should generate a betaal link`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey
        )
        webTestClient
            .get()
            .uri("${path}/generate/link?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.link")
            .isEqualTo("http://localhost:3000?identifier=${identifier}&amount=${amount}&orderid=${orderId}&reference=${reference}&hash=${hash}")
    }

    @Test
    fun `should generate a betaal link with subject`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey
        )

        webTestClient
            .get()
            .uri("${path}/generate/link?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amountInCents}&subject=dit is een test")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.link")
            .isEqualTo("http://localhost:3000?identifier=${identifier}&amount=${amount}&orderid=${orderId}&reference=${reference}&hash=${hash}&subject=dit%20is%20een%20test")
    }

    @Test
    fun `should generate a betaal link, no identifier query string parameter`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey,
        )
        webTestClient
            .get()
            .uri("${path}/generate/link?orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `should generate a betaal link, no api key header`() {
        webTestClient
            .get()
            .uri("${path}/generate/link?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `should generate a betaal link, identifier not configured`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey,
        )
        webTestClient
            .get()
            .uri("${path}/generate/link?identifier=${identifier}1&orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `should generate a betaal link, api key is not equal`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            "12345",
        )
        webTestClient
            .get()
            .uri("${path}/generate/link?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `should generate a betaal qrcode, identifier not configured`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey + "1",
        )
        webTestClient
            .get()
            .uri("${path}/generate/qrcode?identifier=${identifier}1&orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `should generate a betaal qrcode`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey,
        )
        webTestClient
            .get()
            .uri("${path}/generate/qrcode?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amountInCents}")
            .headers {
                it.addAll(headers)
            }.exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.qrcode")
            .isEqualTo("iVBORw0KGgoAAAANSUhEUgAAAZAAAAGQAQAAAACoxAthAAACn0lEQVR4Xu2WSw7cMAxDfQPf/5a+gTv82JN20SIFym6oZGxJ5NNGCDBjv47xa+PPUeRtFHkbRd7GB1mDMfea5/k0z2+4vK4iKWRiQWjappwvB7F7XEVyCCR5ZeELBAr254ZcRbIIPEBwnVqT9KEV+T8INghRHmyQ2jUXSSM0sUQb2DcnQ12uIjEEaxtc1u8eu4qkkBuLm2Pfq6QFX50WqShy458ikpZlAe5NNH7aaJEYsr0onPyBxUnTIe+8IimEhGtcHxNTeCmjxAg6ioSQpU3JgYJG1bCfGSaKxBAU/KG1mDPUMIyKdZEcYu8GuGV0yKoliikSQrQ1YaCmQC+TLaQaWiSGwL5QcFfKVMHMYQ++SAzZlky6gEn5Y+AukkK2Nrlks3yaxHDbMIvEkG27vCrxaACncQSEXSSFPBqLJkvoihGFCt0iGYQSw0aYvjVx1gSLxJCl3KDWxnryHbzF4lckiXB9Ez6y+rRwaZH3QyuSQtQA+ch8socp1otEEYWWeC7j4o9QJIe4ze8JPa0MJhOncqtIDKFA5aq0c8qpoOAuEkKWdqjLD7D5GMUxIotkEO6PDFtcHVU2EWcIsiIpRC7JEqCw5wEC2S2SRLQ8adoqXR5A/Q4pEkIIyHU8KOSiwHTqKRJEBMkqv+0Xsh1RJIZQ5I/K4ueG17OOxqRIBtEK5VExSF3X6QstkkIW/7FBBUZdhUZR8xgwRTLI8drOATgQwGVQUSSHnPjsyxRG8PCL49RFUojXBvs3g892LvJUu0gMwa0TXb1Y3UF5cjTvIinkStqkOkrvLAu7SBzhI9kbRAHXbSItkkYg2KqUyeRXN7hIdIqkkC1lKqcD7wEHAjPZKpJC2JJkmyAfmohU04tkkLdR5G0UeRtF3sZfID8AgAp0E3cn3QIAAAAASUVORK5CYII=")
    }

    @Test
    fun `should generate a betaal qrcode download`() {
        val headers = HttpHeaders()
        headers.add(
            QRLinkPaymentAuthorizationFilter.HEADER_APIKEY,
            qrLinkPaymentModuleConfiguration.properties.getConfiguration(identifier)?.apiKey,
        )
        val responseResult =
            webTestClient
                .get()
                .uri("${path}/generate/qrcode/download?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amountInCents}&filetype=JPG")
                .headers {
                    it.addAll(headers)
                }.exchange()
                .expectStatus()
                .isOk
                .expectHeader()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody()
                .returnResult()

        val responseBodyContent =
            responseResult.responseBodyContent
                ?.toString(Charset.defaultCharset())

        assertNotNull(responseBodyContent)
    }

    @Test
    fun `should do betaling`() {
        webTestClient
            .get()
            .uri("${path}/pay?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amount}&hash=${hash}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.redirectUrl")
            .isEqualTo("https://payment.preprod.direct.worldline-solutions.com/hostedcheckout/PaymentMethods/Selection/e61340e579e04172a740676ffdda162e")
    }

    @Test
    fun `should do betaling tempered data`() {
        webTestClient
            .get()
            .uri("${path}/pay?identifier=${identifier}&orderid=${orderId}&reference=${reference}&amount=${amount}")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `should get status`() {
        webTestClient
            .get()
            .uri("${path}/status?identifier=${identifier}&hostedCheckoutId=4418080728")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.status")
            .isEqualTo("SUCCESSFUL")
    }

    fun setupMockServer() {
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

                            "GET /v2/TAX/hostedcheckouts/4418080728" -> {
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
