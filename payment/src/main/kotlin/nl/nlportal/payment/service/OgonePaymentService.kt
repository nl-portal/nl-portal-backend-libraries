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

import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.payment.autoconfiguration.OgonePaymentConfig
import nl.nlportal.payment.constants.OgoneState
import nl.nlportal.payment.constants.ShaVersion
import nl.nlportal.payment.domain.OgonePayment
import nl.nlportal.payment.domain.OgonePaymentRequest
import nl.nlportal.payment.domain.PaymentField
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakStatus
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ResponseStatusException
import java.security.NoSuchAlgorithmException
import java.util.*

class OgonePaymentService(
    private val paymentConfig: OgonePaymentConfig,
    private val objectsApiTaskConfig: TaakObjectConfig,
    private val objectsApiClient: ObjectsApiClient,
) {
    fun createPayment(paymentRequest: OgonePaymentRequest): OgonePayment {
        val pspId = paymentRequest.pspId
        val paymentProfile =
            paymentConfig.getPaymentProfileByPspPid(pspId)
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Could not found payment profile for the pspId $pspId",
                )
        val payment =
            OgonePayment.create(
                paymentConfig.url,
                paymentProfile,
                paymentRequest,
            )
        val fields = payment.fillFields()
        fields.add(
            PaymentField(
                OgonePayment.PAYMENT_PROPERTY_SHASIGN,
                hashParameters(
                    fields,
                    paymentProfile.shaInKey,
                    paymentProfile.shaVersion,
                ),
            ),
        )
        payment.formFields = fields
        return payment
    }

    suspend fun handlePostSale(serverHttpRequest: ServerHttpRequest): String {
        val pspId = serverHttpRequest.queryParams[OgonePayment.PAYMENT_PROPERTY_PSPID]?.get(0)
        if (!StringUtils.isBlank(pspId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not from payment provider")
        }

        val status = serverHttpRequest.queryParams[OgonePayment.PAYMENT_PROPERTY_STATUS]?.get(0)?.toInt()
        if (status != OgoneState.SUCCESS.status &&
            status != OgoneState.PENDING.status &&
            status != OgoneState.PENDING1.status &&
            status != OgoneState.PENDING2.status
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has not the correct status: $status")
        }

        val orderId = serverHttpRequest.queryParams[OgonePayment.QUERYSTRING_ORDER_ID]?.get(0)
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

        return "Request successful processed"
    }

    private suspend fun getObjectsApiTaak(taskId: UUID): ObjectsApiObject<TaakObjectV2> {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("verwerker_taak_id", Comparator.EQUAL_TO, taskId.toString()),
            )

        return objectsApiClient.getObjects<TaakObjectV2>(
            objectSearchParameters = objectSearchParameters,
            objectTypeUrl = objectsApiTaskConfig.typeUrlV2,
            page = 1,
            pageSize = 2,
        ).results.single()
    }

    private fun isValidOgoneRequest(
        serverHttpRequest: ServerHttpRequest,
        pspId: String,
    ): Boolean {
        val queryStringParameters = serverHttpRequest.queryParams
        val fields = ArrayList<PaymentField>()
        queryStringParameters.forEach {
            // filter out only the accepted parameters
            val uppercaseKey = it.key.uppercase()
            if (paymentConfig.shaOutParameters.contains(uppercaseKey)) {
                fields.add(PaymentField(uppercaseKey, it.value[0]))
            }
        }

        val paymentProfile = paymentConfig.getPaymentProfileByPspPid(pspId) ?: return false
        val hashOutParameter = hashParameters(fields, paymentProfile.shaOutKey, paymentProfile.shaVersion).uppercase()
        val shaOutKey = serverHttpRequest.queryParams[OgonePayment.PAYMENT_PROPERTY_SHASIGN]?.get(0)

        return hashOutParameter == shaOutKey
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        fun hashParameters(
            paymentsParameters: List<PaymentField>,
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
            logger.info("SHA version: {} - {}", shaVersion, parametersConcatenation.toString())
            return createHash(parametersConcatenation.toString(), shaVersion)
        }

        @Throws(NoSuchAlgorithmException::class)
        private fun createHash(
            input: String,
            shaVersion: String,
        ): String {
            return when (shaVersion) {
                ShaVersion.SHA256.version -> {
                    DigestUtils.sha512Hex(input)
                }
                ShaVersion.SHA512.version -> {
                    DigestUtils.sha512Hex(input)
                }
                else -> {
                    DigestUtils.sha1Hex(input)
                }
            }
        }
    }
}