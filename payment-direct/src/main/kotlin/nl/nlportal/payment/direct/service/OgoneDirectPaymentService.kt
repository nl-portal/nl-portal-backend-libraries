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
import com.onlinepayments.defaultimpl.AuthorizationType
import com.onlinepayments.domain.AmountOfMoney
import com.onlinepayments.domain.CreateHostedCheckoutRequest
import com.onlinepayments.domain.HostedCheckoutSpecificInput
import com.onlinepayments.domain.Order
import com.onlinepayments.domain.OrderReferences
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.payment.direct.autoconfiguration.OgoneDirectPaymentModuleConfiguration
import nl.nlportal.payment.direct.constants.OgoneDirectPaymentConstants
import nl.nlportal.payment.direct.constants.OgoneDirectPaymentState
import nl.nlportal.payment.direct.domain.OgoneDirectPaymentField
import nl.nlportal.payment.direct.domain.OgoneDirectPaymentRequest
import nl.nlportal.payment.direct.domain.OgoneDirectPaymentResponse
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakStatus
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*

open class OgoneDirectPaymentService(
    private val ogoneDirectPaymentModuleConfiguration: OgoneDirectPaymentModuleConfiguration,
    private val objectsApiClient: ObjectsApiClient,
) {
    open fun doDirectPayment(paymentRequest: OgoneDirectPaymentRequest): OgoneDirectPaymentResponse {
        try {
            val paymentDirectProfile =
                ogoneDirectPaymentModuleConfiguration.properties.getPaymentProfile(paymentRequest.identifier)
                    ?: ogoneDirectPaymentModuleConfiguration.properties.getPaymentProfileByPspPid(paymentRequest.identifier)
                    ?: throw ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Could not found direct payment profile for the identifier $paymentRequest.identifier",
                    )
            val client =
                Factory.createClient(
                    CommunicatorConfiguration()
                        .withApiKeyId(paymentDirectProfile.apiKey)
                        .withSecretApiKey(paymentDirectProfile.apiSecret)
                        .withApiEndpoint(URI.create(checkAndRemovePath(ogoneDirectPaymentModuleConfiguration.properties.url)))
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
                            )
                            .withReferences(
                                OrderReferences()
                                    .withDescriptor(paymentRequest.reference)
                                    .withMerchantReference(paymentRequest.orderId),
                            ),
                    )
                    .withHostedCheckoutSpecificInput(
                        HostedCheckoutSpecificInput()
                            .withLocale(paymentRequest.langId ?: paymentDirectProfile.language)
                            .withReturnUrl(paymentRequest.returnUrl ?: paymentDirectProfile.returnUrl),
                    )

            val response = merchantClient.hostedCheckout().createHostedCheckout(checkoutRequest)

            return OgoneDirectPaymentResponse(
                redirectUrl = response.redirectUrl,
            )
        } catch (ex: Exception) {
            throw ex
        }
    }

    open suspend fun handlePostSale(serverHttpRequest: ServerHttpRequest): String {
        val orderId = serverHttpRequest.queryParams[OgoneDirectPaymentConstants.QUERYSTRING_ORDER_ID]?.get(0)
        if (isUUID(orderId)) {
            val pspId = serverHttpRequest.queryParams[OgoneDirectPaymentConstants.PAYMENT_PROPERTY_PSPID]?.get(0)
            if (!StringUtils.isBlank(pspId)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not from payment provider")
            }

            val status = serverHttpRequest.queryParams[OgoneDirectPaymentConstants.PAYMENT_PROPERTY_STATUS]?.get(0)?.toInt()
            if (status != OgoneDirectPaymentState.SUCCESS.status &&
                status != OgoneDirectPaymentState.PENDING.status &&
                status != OgoneDirectPaymentState.PENDING1.status &&
                status != OgoneDirectPaymentState.PENDING2.status
            ) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has not the correct status: $status")
            }

            val objectsApiTask = getObjectsApiTaak(UUID.fromString(orderId))
            if (objectsApiTask.record.data.status != TaakStatus.OPEN) {
                return "Task is already completed"
            }

            // validate ogone request
            val pspIdFromTask =
                objectsApiTask.record.data.ogonebetaling?.pspid
                    ?: return "Task does not have a pspId"

            if (!isValidOgoneRequest(serverHttpRequest, pspIdFromTask)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not valid")
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
                HttpStatus.BAD_REQUEST,
                String.format("Taak kan niet gevonden worden", taskId),
            )
        }
        return objectsApiTask
    }

    private fun isValidOgoneRequest(
        serverHttpRequest: ServerHttpRequest,
        pspId: String,
    ): Boolean {
        val queryStringParameters = serverHttpRequest.queryParams
        val fields = ArrayList<OgoneDirectPaymentField>()
        queryStringParameters.forEach {
            // filter out only the accepted parameters
            val uppercaseKey = it.key.uppercase()
            if (ogoneDirectPaymentModuleConfiguration.properties.shaOutParameters.contains(uppercaseKey)) {
                fields.add(OgoneDirectPaymentField(uppercaseKey, it.value[0]))
            }
        }

        val paymentProfile = ogoneDirectPaymentModuleConfiguration.properties.getPaymentProfileByPspPid(pspId) ?: return false
        val hashOutParameter = hashParameters(fields, paymentProfile.shaOutKey, paymentProfile.shaVersion).uppercase()
        val shaOutKey = serverHttpRequest.queryParams[OgoneDirectPaymentConstants.PAYMENT_PROPERTY_SHASIGN]?.get(0)

        return hashOutParameter == shaOutKey
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        fun hashParameters(
            paymentsParameters: List<OgoneDirectPaymentField>,
            shaKey: String,
            shaVersion: String,
        ): String {
            val parametersConcatenation = StringBuilder()
            paymentsParameters
                .sortedBy { it.name }
                .filterNot { field -> field.value.isEmpty() }
                .forEach { field ->
                    parametersConcatenation
                        .append(field.name.uppercase(Locale.getDefault()))
                        .append("=")
                        .append(field.value)
                        .append(shaKey)
                }
            return CoreUtils.createHash(parametersConcatenation.toString(), shaVersion)
        }

        fun isUUID(orderId: String?): Boolean {
            return try {
                UUID.fromString(orderId) != null
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        fun checkAndRemovePath(url: String): String {
            if (url.endsWith("/")) {
                return url.dropLast(1)
            }

            return url
        }
    }
}