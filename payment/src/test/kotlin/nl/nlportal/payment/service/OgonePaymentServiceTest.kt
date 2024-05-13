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
package nl.nlportal.payment.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.payment.autoconfiguration.OgonePaymentConfig
import nl.nlportal.payment.autoconfiguration.OgonePaymentProfile
import nl.nlportal.payment.domain.OgonePaymentRequest
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.server.ResponseStatusException

@ExperimentalCoroutinesApi
internal class OgonePaymentServiceTest {
    val paymentConfig: OgonePaymentConfig = mock()
    val objectsApiClient: ObjectsApiClient = mock()
    val taakObjectConfig: TaakObjectConfig = mock()
    val ogonePaymentService = OgonePaymentService(paymentConfig, taakObjectConfig, objectsApiClient)

    @BeforeEach
    fun setup() {
    }

    @Test
    fun createPaymentTest() {
        val paymentRequest =
            OgonePaymentRequest(
                amount = 100.00,
                orderId = "orderId 123",
                reference = "reference 123",
                title = "title 123",
                langId = "nl_NL",
                successUrl = null,
                failureUrl = null,
                pspId = "TAX",
            )

        val paymentProfile =
            OgonePaymentProfile(
                pspId = "TAX",
                title = "Belastingzaken",
                shaInKey = "de14f0e3-2ff0-45eb-95a6-1cdc35ca7a00",
                failureUrl = "http://dummy.nl",
                successUrl = "http://dummy.nl",
            )

        whenever(paymentConfig.getPaymentProfileByPspPid(anyString())).thenReturn(paymentProfile)
        whenever(paymentConfig.url).thenReturn("https://secure.ogone.com/ncol/prod/orderstandard.asp")

        val payment = ogonePaymentService.createPayment(paymentRequest)
        assertEquals(paymentProfile.pspId, payment.pspId)
        assertEquals(paymentRequest.title, payment.title)
    }

    @Test
    fun createPaymentTestNotFoundPaymentProvider() {
        val paymentRequest =
            OgonePaymentRequest(
                amount = 100.00,
                orderId = "orderId 123",
                reference = "reference 123",
                title = "title 123",
                langId = "nl_NL",
                successUrl = null,
                failureUrl = null,
                pspId = "unknown",
            )

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                runTest {
                    ogonePaymentService.createPayment(paymentRequest)
                }
            }

        assertEquals("Could not found payment profile for the pspId unknown", exception.reason)
    }
}