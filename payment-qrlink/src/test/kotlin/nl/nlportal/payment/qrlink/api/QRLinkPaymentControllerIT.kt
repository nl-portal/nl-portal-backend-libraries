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

import io.github.oshai.kotlinlogging.KotlinLogging
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
            .isEqualTo("iVBORw0KGgoAAAANSUhEUgAAAZAAAAGQAQAAAACoxAthAAAFfklEQVR4Xu2XQa6kOBBE0yuOwU0B37SO4RU58SJNV6vV0kzvRm0Q4hs7IhevIm1+5B9f8evEv1//X8sIXc3PY8tr+xwaxK5BbNk1v0n1Ocd+axXVvrxl11Qfu14132ssGUUY9Px4gEurOV6LxHt/jLeAZ0og4HdoEtQFuXPr53gttuS8OkbmNakc3vZeM7Fz6bU8FrWwBgqhOevPNglrXik9fwN5UUsCVpNfYxWR4MCCveWn+SfI18IWB7r+H27If8+XX1d/e/+FFiiC7g5uBpvSaKSKImw/Z7IN9lLm8w2zsIUQFkCdodKMPauRyWccfmqJCbZBIC9uScByFoDXqH3Ifkp2DpM35BKsbhlaNWfNmHZ3DhMB1a7gqgqN1Vzesns9J1iF0MC789nhr28S5qnp75PVLdZ3n6pyBeH0jErpeA12v9J3V+jPbrmuxcE7JtX6ThNYOGe5nga/RFg1aeTFLQKrGS0RuT7BerDR2kkLp561err3l7YkMH/CCGEEepV+kNXDq53ihHZ1i0PoKH6a2Cb6s5D6qev2rnhQIcJJXtui5o0DkgDEQmsXUhtrVTMqNZAtb9lFNQyw8WVingD/uI67eKC3XSnld1na4ijezmQ6darQnig2/wQnryLvwUbvL20RQG6y14ginM9hI0/O2Ys6FOF6LSNAuhFCDWTPwakR3GCXUYTvamSHc3lLQZaMyF0RFn9ifpagbMnMMXc/en9tS4WQ+GUNAs7CjqXmUeonMORY3qJMxo7Mgu7drxM/LIiR+dSocI7lLQPByabnJXsvLyGgx93mnCbG/lrcpCIbTDJDBdPWM2cXz9CqiCqsbhmKnF6Ftzq6BDy725ZkWnxZn976lraA0WwHpE9ZWCWfsrQSE8tZVuLlLbTtvfH0ZD19b1Vqcr7c0SeQ17YMf+tC0l6LedVDYF3wwlvzTK5uMUCBDQBOyOFtUOJOQf0E3gBZIqWv5XT2en7AqOyRUlr4slGJdZGK6BfyypYCewJTAmC6AocFFTx5mvkPyEtbiKIuOrfNKOLVa2w+I2SssWV6X94Ct5slgpc+bfUqyCJ/eD/MSqyrJYfF6pbLSJst3WdHo7sFnyVdqqmINuAT1NcCQ40lEO2oBCLuVLMy6uOEIjeNvLYFivWqC4yGmdckP//JKuzcauTVLWhyQPiqvW5wUghsM2TAmnBsdLchL27RNV0aGayXCvKPZOqJjHyubuFVYMOZLPFH4277AXmKUm2rOj5flrZITJ+KZJ9I6VaPkfX6ILE9zf+1NHO+ATv1V2iX8yRUKXi6cvjrd3nLfvst2OVMm9tst9rx0B+I0TuWq1s6DOfWd28oNZ+WqdJh/vwELq5fZHWL9fUa7HIkM7CoFKhlbACHcGleC6/DkJ8Q6illsPXxKnEf4VIwfy3MGOajpGcPr/YKKuO52vK1cCLcyBAI5vGATV4h7wr7FXHqzNXWt7jFIWxARnBDMlPKJJN0tI38CoH39Im8uOU053zm2wBsYw9kJlBqFfjN4tfyLBE8PeGszmUgqZh7iZpycfIub9n7A1Bgg4g++aQIq0AmkIY8lre4YRszpczbLlG92A8xdqrxKvgarG6xLIc2QHoZtnOpzlPqCPEdfPfaxe+ytIWwuYW3jx6ngwdhuahjYyi3Lsvka/GFDLzNFoHtSqmyqlcn8y7aA9SrWwpdVs/CEJh4TT4YH3Q0yexUyOUte8Iz7+0T7la95gOZaoxZmpC31yKY0KulY5tKxLzSyJQilp4c7Javxa+a9y4XwOQmq1liTV7wV4Uv5MUthw8IU00nsE7YvGhkXNgRvBahI4oa3ybZXcSWTHtPeYMnrtdCIGs1hFSaG7Dc5b0p9QXevufLqpb84+tvsvwDPw+ZXeX+tLYAAAAASUVORK5CYII=")
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
