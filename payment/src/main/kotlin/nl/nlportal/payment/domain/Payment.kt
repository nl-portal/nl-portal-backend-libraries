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
package nl.nlportal.payment.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import nl.nlportal.payment.autoconfiguration.PaymentProfile
import java.math.BigDecimal

data class Payment(
    @JsonIgnore val pspId: String,
    @JsonIgnore val amount: BigDecimal = BigDecimal.ZERO,
    @JsonIgnore val title: String,
    @JsonIgnore val reference: String,
    @JsonIgnore var orderId: String,
    @JsonIgnore val currency: String,
    @JsonIgnore val language: String,
    @JsonIgnore val acceptUrl: String,
    @JsonIgnore val declineUrl: String,
    @JsonIgnore val exceptionUrl: String,
    @JsonIgnore val cancelUrl: String,
    val formAction: String,
    var fields: MutableMap<String, String?> = mutableMapOf(),
) {
    /**
     * amount is converted to cents to avoid decimals
     */
    fun fillFields(): MutableMap<String, String?> {
        return mutableMapOf(
            PAYMENT_PROPERTY_ACCEPT_URL to acceptUrl,
            PAYMENT_PROPERTY_CANCEL_URL to cancelUrl,
            PAYMENT_PROPERTY_DECLINE_URL to declineUrl,
            PAYMENT_PROPERTY_EXCEPTION_URL to exceptionUrl,
            PAYMENT_PROPERTY_CURRENCY to currency,
            PAYMENT_PROPERTY_LANGUAGE to language,
            PAYMENT_PROPERTY_PSPID to pspId,
            PAYMENT_ORDER_ID to orderId,
            PAYMENT_COM to reference,
            PAYMENT_AMOUNT to amount.movePointRight(2).toString(),
            PAYMENT_TITLE to title,
        )
    }

    companion object {
        fun create(
            paymentUrl: String,
            paymentProfile: PaymentProfile,
            paymentRequest: PaymentRequest,
        ): Payment {
            val successUrl =
                paymentRequest.successUrl
                    ?: (paymentProfile.successUrl + QUERYSTRING_ORDER_ID + paymentRequest.orderId)
            val failureUrl =
                paymentRequest.failureUrl
                    ?: (paymentProfile.failureUrl + QUERYSTRING_ORDER_ID + paymentRequest.orderId)
            return Payment(
                pspId = paymentProfile.pspId,
                currency = paymentProfile.currency,
                language = paymentRequest.langId ?: paymentProfile.language,
                title = paymentRequest.title ?: paymentProfile.title,
                acceptUrl = successUrl,
                declineUrl = failureUrl,
                exceptionUrl = failureUrl,
                cancelUrl = failureUrl,
                formAction = paymentUrl,
                reference = paymentRequest.reference,
                amount = paymentRequest.amount,
                orderId = paymentRequest.orderId,
            )
        }

        const val PAYMENT_PROPERTY_ACCEPT_URL: String = "ACCEPTURL"
        const val PAYMENT_PROPERTY_CANCEL_URL: String = "CANCELURL"
        const val PAYMENT_PROPERTY_DECLINE_URL: String = "DECLINEURL"
        const val PAYMENT_PROPERTY_EXCEPTION_URL: String = "EXCEPTIONURL"
        const val PAYMENT_PROPERTY_CURRENCY: String = "CURRENCY"
        const val PAYMENT_PROPERTY_LANGUAGE: String = "LANGUAGE"
        const val PAYMENT_PROPERTY_PSPID: String = "PSPID"
        const val PAYMENT_ORDER_ID: String = "ORDERID"
        const val PAYMENT_COM: String = "COM"
        const val PAYMENT_AMOUNT: String = "AMOUNT"
        const val PAYMENT_TITLE: String = "TITLE"
        const val PAYMENT_SHASIGN: String = "SHASIGN"
        const val QUERYSTRING_ORDER_ID: String = "?orderId="
    }
}