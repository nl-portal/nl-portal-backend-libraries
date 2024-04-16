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
package nl.nlportal.payment.graphql

import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.payment.autoconfiguration.OgonePaymentConfig
import nl.nlportal.payment.domain.OgonePayment
import nl.nlportal.payment.domain.OgonePaymentRequest
import nl.nlportal.payment.service.OgonePaymentService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.function.Consumer

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class OgonePaymentMutationIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val paymentConfig: OgonePaymentConfig,
) {
    @MockBean
    lateinit var reactiveJwtDecoder: ReactiveJwtDecoder

    @Test
    @WithBurgerUser("123")
    fun generateOgonePayment() {
        val paymentRequest =
            OgonePaymentRequest(
                pspId = "TAX",
                amount = 100.25,
                orderId = "123456",
                reference = "12345",
                title = "Gemeente belastingen 2024",
                langId = null,
                successUrl = null,
                failureUrl = null,
            )
        val payment =
            OgonePayment.create(
                paymentConfig.url,
                paymentConfig.getPaymentProfile("belastingzaken")!!,
                paymentRequest,
            )

        val shaSign =
            OgonePaymentService.hashParameters(
                payment.fillFields(),
                paymentConfig.getPaymentProfile("belastingzaken")!!.shaOutKey,
                paymentConfig.getPaymentProfile("belastingzaken")!!.shaVersion,
            )
        val mutation =
            """
            mutation {
                generateOgonePayment(
                    paymentRequest: { pspId: "TAX", amount: 100.25, orderId: "123456", reference: "12345", title: "Gemeente belastingen 2024" }
                ) {
                formAction,
                formFields{
                    name,
                    value
                }
                }
            }
            """.trimIndent()

        val basePath = "$.data.generateOgonePayment"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(mutation)
            .exchange()
            .expectBody()
            .consumeWith(Consumer { t -> logger.info { t } })
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.formFields[0].value").isEqualTo("http://localhost:3000?orderId=123456")
            .jsonPath("$basePath.formFields[9].value").isEqualTo("10025")
            .jsonPath("$basePath.formFields[11].value").isEqualTo(shaSign)
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}