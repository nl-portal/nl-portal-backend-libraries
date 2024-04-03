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

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.fasterxml.jackson.annotation.JsonIgnore
import nl.nlportal.payment.autoconfiguration.PaymentProfile
import java.math.BigDecimal

data class Payment(
    @GraphQLIgnore @JsonIgnore val pspId: String,
    @GraphQLIgnore @JsonIgnore val amount: BigDecimal = BigDecimal.ZERO,
    @GraphQLIgnore @JsonIgnore val title: String,
    @GraphQLIgnore @JsonIgnore val reference: String,
    @GraphQLIgnore @JsonIgnore var orderId: String,
    @GraphQLIgnore @JsonIgnore val currency: String,
    @GraphQLIgnore @JsonIgnore val language: String,
    @GraphQLIgnore @JsonIgnore val acceptUrl: String,
    @GraphQLIgnore @JsonIgnore val declineUrl: String,
    @GraphQLIgnore @JsonIgnore val exceptionUrl: String,
    @GraphQLIgnore @JsonIgnore val cancelUrl: String,
    val formAction: String,
    var formFields: ArrayList<PaymentField>,
) {
    /**
     * amount is converted to cents to avoid decimals
     */
    @GraphQLIgnore
    fun fillFields(): ArrayList<PaymentField> {
        val fields = ArrayList<PaymentField>()
        fields.add(PaymentField(PAYMENT_PROPERTY_ACCEPT_URL, acceptUrl))
        fields.add(PaymentField(PAYMENT_PROPERTY_CANCEL_URL, cancelUrl))
        fields.add(PaymentField(PAYMENT_PROPERTY_DECLINE_URL, declineUrl))
        fields.add(PaymentField(PAYMENT_PROPERTY_EXCEPTION_URL, exceptionUrl))
        fields.add(PaymentField(PAYMENT_PROPERTY_CURRENCY, currency))
        fields.add(PaymentField(PAYMENT_PROPERTY_LANGUAGE, language))
        fields.add(PaymentField(PAYMENT_PROPERTY_PSPID, pspId))
        fields.add(PaymentField(PAYMENT_PROPERTY_ORDER_ID, orderId))
        fields.add(PaymentField(PAYMENT_PROPERTY_COM, reference))
        fields.add(PaymentField(PAYMENT_PROPERTY_AMOUNT, amount.movePointRight(2).toString()))
        fields.add(PaymentField(PAYMENT_PROPERTY_TITLE, title))
        return fields
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
                amount = paymentRequest.amount.toBigDecimal(),
                orderId = paymentRequest.orderId,
                formFields = ArrayList(),
            )
        }

        const val PAYMENT_PROPERTY_ACCEPT_URL: String = "ACCEPTURL"
        const val PAYMENT_PROPERTY_CANCEL_URL: String = "CANCELURL"
        const val PAYMENT_PROPERTY_DECLINE_URL: String = "DECLINEURL"
        const val PAYMENT_PROPERTY_EXCEPTION_URL: String = "EXCEPTIONURL"
        const val PAYMENT_PROPERTY_CURRENCY: String = "CURRENCY"
        const val PAYMENT_PROPERTY_LANGUAGE: String = "LANGUAGE"
        const val PAYMENT_PROPERTY_PSPID: String = "PSPID"
        const val PAYMENT_PROPERTY_ORDER_ID: String = "ORDERID"
        const val PAYMENT_PROPERTY_COM: String = "COM"
        const val PAYMENT_PROPERTY_AMOUNT: String = "AMOUNT"
        const val PAYMENT_PROPERTY_TITLE: String = "TITLE"
        const val PAYMENT_PROPERTY_SHASIGN: String = "SHASIGN"
        const val QUERYSTRING_ORDER_ID: String = "?orderId="
    }
}

data class PaymentField(
    val name: String,
    val value: String,
)