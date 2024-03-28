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
import nl.nlportal.payment.TestHelper.logRestResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.function.Consumer

@SpringBootTest
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentControllerIT(
    @Autowired private val webTestClient: WebTestClient,
) {
    @Test
    fun paymentRequestTest() {
        val requestBody =
            """
               {
                    "amount": 100,
                    "orderId": "123456",
                    "reference": "12345",
                    "title": "Gemeente belastingen 2024"
                } 
            """
        webTestClient.post()
            .uri("/api/payment/belastingzaken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isOk
            .logRestResponse()
            .jsonPath("formAction").isEqualTo("https://secure.ogone.com/ncol/test/orderstandard.asp")
            .jsonPath("fields.AMOUNT").isEqualTo("10000")
            .jsonPath(
                "fields.SHASIGN",
            ).isNotEmpty()
    }

    @Test
    fun paymentRequestTestNotFoundPaymentProvider() {
        val requestBody =
            """
               {
                    "amount": 100,
                    "orderId": "123456",
                    "reference": "12345",
                    "title": "Gemeente belastingen 2024"
                } 
            """
        webTestClient.post()
            .uri("/api/payment/unkown")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .consumeWith(Consumer { t -> TestHelper.logger.info { t } })
            .returnResult()
            .responseBody
            .contentToString()
            .contains("Could not found payment provider for the identifier unkown")
    }
}