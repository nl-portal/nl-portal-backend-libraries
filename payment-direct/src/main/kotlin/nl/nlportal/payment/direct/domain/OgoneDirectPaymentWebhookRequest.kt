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
package nl.nlportal.payment.direct.domain

data class OgoneDirectPaymentWebhookRequest(
    val merchantId: String,
    val id: String,
    val type: String,
    val payment: OgoneDirectPayment,
)

data class OgoneDirectPayment(
    val status: String,
    val statusOutput: OgoneDirectPaymentStatusOutput,
    val paymentOutput: OgoneDirectPaymentOutput,
)

data class OgoneDirectPaymentStatusOutput(
    val statusCode: Int,
)

data class OgoneDirectPaymentOutput(
    val amountOfMoney: OgoneDirectPaymentAmountOfMoney,
    val references: OgoneDirectPaymentReference,
)

data class OgoneDirectPaymentAmountOfMoney(
    val amount: Long,
    val currencyCode: String,
)

data class OgoneDirectPaymentReference(
    val merchantReference: String,
)