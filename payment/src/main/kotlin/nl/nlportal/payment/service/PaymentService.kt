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

import nl.nlportal.payment.autoconfiguration.PaymentConfig
import nl.nlportal.payment.autoconfiguration.PaymentProfile
import nl.nlportal.payment.domain.Payment
import nl.nlportal.payment.domain.Payment.Companion.PAYMENT_SHASIGN
import nl.nlportal.payment.domain.PaymentRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class PaymentService(
    private val paymentConfig: PaymentConfig,
) {
    fun createPayment(
        paymentRequest: PaymentRequest,
        paymentProfileIdentifier: String,
    ): Payment {
        val paymentProfile =
            paymentConfig.getPaymentProfile(paymentProfileIdentifier)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not found payment profile for the identifier $paymentProfileIdentifier")
        val payment =
            Payment.create(
                paymentConfig.url,
                paymentProfile,
                paymentRequest,
            )
        val fields = payment.fillFields()
        fields[PAYMENT_SHASIGN] = hashParameters(fields, paymentProfile)
        payment.fields = fields
        return payment
    }

    private fun hashParameters(
        paymentsParameters: Map<String, String?>,
        paymentProfile: PaymentProfile,
    ): String {
        val parametersConcatenation = StringBuilder()

        paymentsParameters.forEach { (key, value) ->
            parametersConcatenation
                .append(key.uppercase(Locale.getDefault()))
                .append("=")
                .append(value)
                .append(paymentProfile.shaInKey)
        }
        return hashSHA512(parametersConcatenation.toString())
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun hashSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        digest.reset()
        digest.update(input.toByteArray(StandardCharsets.UTF_8))
        return String.format("%0128x", BigInteger(1, digest.digest()))
    }
}