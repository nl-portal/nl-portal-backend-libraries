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
import nl.nlportal.payment.autoconfiguration.PaymentConfig
import nl.nlportal.payment.autoconfiguration.PaymentProfile
import nl.nlportal.payment.constants.OgoneState
import nl.nlportal.payment.domain.Payment
import nl.nlportal.payment.domain.PaymentField
import nl.nlportal.payment.domain.PaymentRequest
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.domain.TaakObject
import nl.nlportal.zgw.taak.domain.TaakStatus
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class PaymentService(
    private val paymentConfig: PaymentConfig,
    private val objectsApiClient: ObjectsApiClient,
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
        fields.add(PaymentField(Payment.PAYMENT_PROPERTY_SHASIGN, hashParameters(fields, paymentProfile)))
        payment.formFields = fields
        return payment
    }

    suspend fun handlePostSale(
        orderId: String,
        pspId: String?,
        status: Int,
    ): String {
        // Check if request is from the Ogone server, if pspId is empty the request is from Ogone
        val requestFromOgoneServer = StringUtils.isBlank(pspId)
        if (!requestFromOgoneServer) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not from payment provider")
        }

        if (status != OgoneState.SUCCESS.status) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has not the correct status")
        }
        val objectsApiTask = getObjectsApiTaak(UUID.fromString(orderId))
        if (objectsApiTask.record.data.status == TaakStatus.INGEDIEND) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Task is already finished - orderId: $orderId")
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

    private fun hashParameters(
        paymentsParameters: List<PaymentField>,
        paymentProfile: PaymentProfile,
    ): String {
        val parametersConcatenation = StringBuilder()

        paymentsParameters.forEach { field ->
            parametersConcatenation
                .append(field.name.uppercase(Locale.getDefault()))
                .append("=")
                .append(field.value)
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

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}