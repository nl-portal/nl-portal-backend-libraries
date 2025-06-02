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
package nl.nlportal.payment.direct.service

import com.onlinepayments.CommunicatorConfiguration
import com.onlinepayments.Factory
import com.onlinepayments.authentication.AuthorizationType
import com.onlinepayments.communication.RequestHeader
import com.onlinepayments.domain.AmountOfMoney
import com.onlinepayments.domain.CreateHostedCheckoutRequest
import com.onlinepayments.domain.Feedbacks
import com.onlinepayments.domain.HostedCheckoutSpecificInput
import com.onlinepayments.domain.Order
import com.onlinepayments.domain.OrderReferences
import com.onlinepayments.json.DefaultMarshaller
import com.onlinepayments.webhooks.InMemorySecretKeyStore
import com.onlinepayments.webhooks.SignatureValidationException
import com.onlinepayments.webhooks.WebhooksHelper
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.util.Mapper
import nl.nlportal.payment.direct.autoconfiguration.DirectPaymentModuleConfiguration
import nl.nlportal.payment.direct.constants.DirectPaymentState
import nl.nlportal.payment.direct.domain.DirectPaymentRequest
import nl.nlportal.payment.direct.domain.DirectPaymentResponse
import nl.nlportal.payment.direct.domain.DirectPaymentWebhookRequest
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*

open class DirectPaymentService(
    private val directPaymentModuleConfiguration: DirectPaymentModuleConfiguration,
    private val objectsApiClient: ObjectsApiClient,
) {
    val webhookHelper = WebhooksHelper(DefaultMarshaller.INSTANCE, InMemorySecretKeyStore.INSTANCE)

    init {
        directPaymentModuleConfiguration.properties.configurations.forEach {
            InMemorySecretKeyStore.INSTANCE.storeSecretKey(it.value.webhookApiKey, it.value.webhookApiSecret)
        }
    }

    /**
     * Do Direct Payment at payment provider
     * @param paymentRequest: properties to create a payment
     * @return redirectUrl to do the actual payment at payment provider
     */
    open fun doDirectPayment(paymentRequest: DirectPaymentRequest): DirectPaymentResponse {
        try {
            val paymentDirectProfile =
                directPaymentModuleConfiguration.properties.getPaymentProfile(paymentRequest.identifier)
                    ?: directPaymentModuleConfiguration.properties.getPaymentProfileByPspPid(paymentRequest.identifier)
                    ?: throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Could not found direct payment profile for the identifier $paymentRequest.identifier",
                    )
            val client =
                Factory.createClient(
                    CommunicatorConfiguration()
                        .withApiKeyId(paymentDirectProfile.apiKey)
                        .withSecretApiKey(paymentDirectProfile.apiSecret)
                        .withApiEndpoint(URI.create(checkAndRemovePath(directPaymentModuleConfiguration.properties.url)))
                        .withIntegrator(paymentRequest.identifier)
                        .withAuthorizationType(AuthorizationType.V1HMAC),
                )
            val merchantClient = client.merchant(paymentDirectProfile.pspId)

            val checkoutRequest =
                CreateHostedCheckoutRequest()
                    .withOrder(
                        Order()
                            .withAmountOfMoney(
                                AmountOfMoney()
                                    .withAmount((paymentRequest.amount * 100).toLong())
                                    .withCurrencyCode(paymentDirectProfile.currency),
                            ).withReferences(
                                OrderReferences()
                                    .withDescriptor(paymentRequest.reference)
                                    .withMerchantReference(paymentRequest.orderId),
                            ),
                    ).withHostedCheckoutSpecificInput(
                        HostedCheckoutSpecificInput()
                            .withLocale(paymentRequest.langId ?: paymentDirectProfile.language)
                            .withReturnUrl(paymentRequest.returnUrl ?: paymentDirectProfile.returnUrl),
                    )

            // if webhookUrl property is configured, set webhook url in the request. Benefit is dynamically set the webhook url per environment.
            directPaymentModuleConfiguration.properties.webhookUrl?.let {
                checkoutRequest.withFeedbacks(
                    Feedbacks().withWebhookUrl(it),
                )
            }

            val response = merchantClient.hostedCheckout().createHostedCheckout(checkoutRequest)

            return DirectPaymentResponse(
                redirectUrl = response.redirectUrl,
            )
        } catch (ex: Exception) {
            throw ex
        }
    }

    /**
     * Handle Postsale webhook server to server call from payment provider
     * @param: httpHeaders, headers from the request
     * @param: jsonBody, the raw body of the request
     *
     * @return: information which will used in the response of the webhook
     * @throws: ResponseStatusException is some check is not valid
     */
    open suspend fun handlePostSale(
        httpHeaders: HttpHeaders,
        jsonBody: String,
    ): String {
        if (!isValidDirectRequest(httpHeaders, jsonBody)) {
            return "Request is not valid"
        }
        val ogoneDirectPaymentWebhookRequest =
            Mapper.get().readValue(
                jsonBody,
                DirectPaymentWebhookRequest::class.java,
            )
        val orderId = ogoneDirectPaymentWebhookRequest.payment.paymentOutput.references.merchantReference
        if (isUUID(orderId)) {
            val status = ogoneDirectPaymentWebhookRequest.payment.statusOutput.statusCode
            if (status != DirectPaymentState.SUCCESS.status &&
                status != DirectPaymentState.PENDING.status &&
                status != DirectPaymentState.PENDING1.status &&
                status != DirectPaymentState.PENDING2.status
            ) {
                return "Request has not the correct status: $status"
            }

            val objectsApiTask = getObjectsApiTaak(UUID.fromString(orderId))
            if (objectsApiTask.record.data.status != TaakStatus.OPEN) {
                return "Task is already completed"
            }

            val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
            updateRequest.record.data.status = TaakStatus.AFGEROND
            updateRequest.record.correctedBy = "Payment provider"
            updateRequest.record.correctionFor = objectsApiTask.record.index.toString()
            objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)
        }
        return "Request successful processed for order $orderId"
    }

    private suspend fun getObjectsApiTaak(taskId: UUID): ObjectsApiObject<TaakObjectV2> {
        val objectsApiTask = objectsApiClient.getObjectById<TaakObjectV2>(taskId.toString())
        if (objectsApiTask == null) {
            throw ResponseStatusException(
                HttpStatus.OK,
                String.format("Taak kan niet gevonden worden", taskId),
            )
        }
        return objectsApiTask
    }

    private fun isValidDirectRequest(
        httpHeaders: HttpHeaders,
        bodyOfRequest: String,
    ): Boolean {
        val requestHeaders = mutableListOf<RequestHeader>()
        httpHeaders.forEach {
            if (directPaymentModuleConfiguration.properties.webhookHeaders.contains(it.key)) {
                requestHeaders.add(RequestHeader(it.key, it.value[0]))
            }
        }

        try {
            webhookHelper.unmarshal(bodyOfRequest, requestHeaders)
            return true
        } catch (e: SignatureValidationException) {
            return false
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        fun isUUID(orderId: String?): Boolean =
            try {
                UUID.fromString(orderId) != null
            } catch (e: IllegalArgumentException) {
                false
            }

        fun checkAndRemovePath(url: String): String {
            if (url.endsWith("/")) {
                return url.dropLast(1)
            }

            return url
        }
    }
}