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

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester

@Deprecated("Is not supported anymore at payment provider, use Direct payment")
@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class OgonePaymentMutationIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val paymentConfig: OgonePaymentConfig,
) {
    @Test
    @WithBurgerUser("123")
    fun generateOgonePaymentWithIdentifier() {
        val paymentRequest =
            OgonePaymentRequest(
                pspId = "belastingzaken",
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
                paymentConfig.properties.url,
                paymentConfig.properties.getPaymentProfile("belastingzaken")!!,
                paymentRequest,
            )

        val shaSign =
            OgonePaymentService.hashParameters(
                payment.fillFields(),
                paymentConfig.properties.getPaymentProfile("belastingzaken")!!.shaOutKey,
                paymentConfig.properties.getPaymentProfile("belastingzaken")!!.shaVersion,
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

        val responseBody =
            httpGraphQlTester
                .document(mutation)
                .execute()
                .errors()
                .verify()
                .path("generateOgonePayment")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("http://localhost:3000", responseBody.requiredAt("/formFields/0/value")?.textValue())
        assertEquals("10025", responseBody.requiredAt("/formFields/9/value")?.textValue())
        assertEquals(shaSign, responseBody.requiredAt("/formFields/11/value")?.textValue())
    }

    @Test
    @WithBurgerUser("123")
    fun generateOgonePaymentWithPspId() {
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
                paymentConfig.properties.url,
                paymentConfig.properties.getPaymentProfile("belastingzaken")!!,
                paymentRequest,
            )

        val shaSign =
            OgonePaymentService.hashParameters(
                payment.fillFields(),
                paymentConfig.properties.getPaymentProfile("belastingzaken")!!.shaOutKey,
                paymentConfig.properties.getPaymentProfile("belastingzaken")!!.shaVersion,
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

        val responseBody =
            httpGraphQlTester
                .document(mutation)
                .execute()
                .errors()
                .verify()
                .path("generateOgonePayment")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("http://localhost:3000", responseBody.requiredAt("/formFields/0/value")?.textValue())
        assertEquals("10025", responseBody.requiredAt("/formFields/9/value")?.textValue())
        assertEquals(shaSign, responseBody.requiredAt("/formFields/11/value")?.textValue())
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}