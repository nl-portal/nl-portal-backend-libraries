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
import nl.nlportal.zgw.taak.domain.TaakObject
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
        var pspId = serverHttpRequest.queryParams[OgonePayment.PAYMENT_PROPERTY_PSPID]?.get(0)
        // Check if request is from the Ogone server, if pspId is empty the request is from Ogone
        val requestFromOgoneServer = StringUtils.isBlank(pspId)
        if (!requestFromOgoneServer) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not from payment provider")
        }

        val status = serverHttpRequest.queryParams[OgonePayment.PAYMENT_PROPERTY_STATUS]?.get(0)?.toInt()
        if (status != OgoneState.SUCCESS.status) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has not the correct status")
        }

        val orderId = serverHttpRequest.queryParams[OgonePayment.PAYMENT_PROPERTY_ORDER_ID]?.get(0)
        val objectsApiTask = getObjectsApiTaak(UUID.fromString(orderId))
        if (objectsApiTask.record.data.status == TaakStatus.INGEDIEND) {
            throw ResponseStatusException(HttpStatus.OK)
        }

        // validate ogone request
        val pspIdFromTask = objectsApiTask.record.data.data[OgonePayment.PAYMENT_PROPERTY_PSPID.lowercase()]
        if (pspIdFromTask == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not have a pspId")
        }

        if (!isValidOgoneRequest(serverHttpRequest, pspIdFromTask as String)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not valid")
        }

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
        updateRequest.record.data.status = TaakStatus.INGEDIEND
        objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)

        return "Request Successful processed"
    }

    private suspend fun getObjectsApiTaak(taskId: UUID): ObjectsApiObject<TaakObject> {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("verwerker_taak_id", Comparator.EQUAL_TO, taskId.toString()),
            )

        return objectsApiClient.getObjects<TaakObject>(
            objectSearchParameters = objectSearchParameters,
            objectTypeUrl = paymentConfig.taakTypeUrl,
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
            if (paymentConfig.shaOutParameters.contains(it.key)) {
                fields.add(PaymentField(it.key, it.value[0]))
            }
        }

        val paymentProfile = paymentConfig.getPaymentProfileByPspPid(pspId) ?: return false
        val hashOutParameter = hashParameters(fields, paymentProfile.shaOutKey, paymentProfile.shaVersion)
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

            paymentsParameters.forEach { field ->
                parametersConcatenation
                    .append(field.name.uppercase(Locale.getDefault()))
                    .append("=")
                    .append(field.value)
                    .append(shaKey)
            }
            return createHash(parametersConcatenation.toString(), shaVersion)
        }

        @Throws(NoSuchAlgorithmException::class)
        private fun createHash(
            input: String,
            shaVersion: String,
        ): String {
            return when (shaVersion) {
                ShaVersion.SHA256.version -> {
                    DigestUtils.sha256Hex(input)
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