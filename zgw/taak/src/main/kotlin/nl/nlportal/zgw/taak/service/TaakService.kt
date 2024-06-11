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
package nl.nlportal.zgw.taak.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import nl.nlportal.zgw.taak.domain.Taak
import nl.nlportal.zgw.taak.domain.TaakIdentificatie
import nl.nlportal.zgw.taak.domain.TaakObject
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakStatus
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.graphql.TaakPage
import nl.nlportal.zgw.taak.graphql.TaakPageV2
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

open class TaakService(
    private val objectsApiClient: ObjectsApiClient,
    private val objectsApiTaskConfig: TaakObjectConfig,
) {
    @Deprecated("Use version 2")
    suspend fun getTaken(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
    ): TaakPage {
        return getTakenResultPage<TaakObject>(
            pageNumber,
            pageSize,
            authentication,
            zaakUUID,
            objectsApiTaskConfig.typeUrl,
        ).let { TaakPage.fromResultPage(pageNumber, pageSize, it) }
    }

    suspend fun getTakenV2(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
    ): TaakPageV2 {
        return getTakenResultPage<TaakObjectV2>(
            pageNumber,
            pageSize,
            authentication,
            zaakUUID,
            objectsApiTaskConfig.typeUrlV2 ?: "",
        ).let { TaakPageV2.fromResultPage(pageNumber, pageSize, it) }
    }

    @Deprecated("Use version 2")
    suspend fun getTaakById(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): Taak {
        val taak =
            Taak.fromObjectsApiTask(
                getObjectsApiTaak<TaakObject>(
                    id,
                    authentication,
                    objectsApiTaskConfig.typeUrl,
                ),
            )
        // do validation if the user is authenticated for this task
        val isAuthorized = isAuthorizedForTaak(authentication, taak.identificatie)
        if (isAuthorized) {
            return taak
        }
        throw IllegalStateException("Access denied to this taak")
    }

    suspend fun getTaakByIdV2(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): TaakV2 {
        val taak =
            TaakV2.fromObjectsApi(
                getObjectsApiTaak<TaakObjectV2>(
                    id,
                    authentication,
                    objectsApiTaskConfig.typeUrl,
                ),
            )
        // do validation if the user is authenticated for this task
        val isAuthorized = isAuthorizedForTaak(authentication, taak.identificatie)
        if (isAuthorized) {
            return taak
        }
        throw IllegalStateException("Access denied to this taak")
    }

    @Deprecated("Use version 2")
    suspend fun submitTaak(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): Taak {
        val objectsApiTask =
            getObjectsApiTaak<TaakObject>(
                id,
                authentication,
                objectsApiTaskConfig.typeUrl,
            )
        if (objectsApiTask.record.data.status != TaakStatus.OPEN) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("Status is niet open, taak [%s] kan niet afgerond worden", id),
            )
        }
        val submissionAsMap = Mapper.get().convertValue(submission, object : TypeReference<Map<String, Any>>() {})

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
        updateRequest.record.data.verzondenData = submissionAsMap
        updateRequest.record.data.status = TaakStatus.INGEDIEND
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiTask.record.index.toString()

        val updatedObjectsApiTask = objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)
        return Taak.fromObjectsApiTask(updatedObjectsApiTask)
    }

    suspend fun submitTaakV2(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): TaakV2 {
        val objectsApiTask =
            getObjectsApiTaak<TaakObjectV2>(
                id,
                authentication,
                objectsApiTaskConfig.typeUrl,
            )
        if (objectsApiTask.record.data.status != TaakStatus.OPEN) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("Status is niet open, taak [%s] kan niet afgerond worden", id),
            )
        }
        val submissionAsMap = Mapper.get().convertValue(submission, object : TypeReference<Map<String, Any>>() {})

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
        updateRequest.record.data.formtaak?.verzondenData = submissionAsMap
        updateRequest.record.data.status = TaakStatus.AFGEROND
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiTask.record.index.toString()

        val updatedObjectsApiTask = objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)
        return TaakV2.fromObjectsApi(updatedObjectsApiTask)
    }

    private suspend inline fun <reified T> getObjectsApiTaak(
        taskId: UUID,
        authentication: CommonGroundAuthentication,
        objectTypeUrl: String,
    ): ObjectsApiObject<T> {
        val userSearchParameters = getUserSearchParameters(authentication)
        val taskIdSearchParameter = ObjectSearchParameter("verwerker_taak_id", Comparator.EQUAL_TO, taskId.toString())

        return objectsApiClient.getObjects<T>(
            objectSearchParameters = userSearchParameters + taskIdSearchParameter,
            objectTypeUrl = objectTypeUrl,
            page = 1,
            pageSize = 2,
        ).results.single()
    }

    private suspend inline fun <reified T> getTakenResultPage(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
        objectTypeUrl: String,
    ): ResultPage<ObjectsApiObject<T>> {
        val objectSearchParameters = mutableListOf<ObjectSearchParameter>()

        objectSearchParameters.addAll(getUserSearchParameters(authentication))
        objectSearchParameters.add(ObjectSearchParameter("status", Comparator.EQUAL_TO, "open"))

        zaakUUID?.let {
            objectSearchParameters.add(
                ObjectSearchParameter(
                    "zaak",
                    Comparator.STRING_CONTAINS,
                    it.toString(),
                ),
            )
        }
        return objectsApiClient.getObjects<T>(
            objectSearchParameters = objectSearchParameters,
            objectTypeUrl = objectTypeUrl,
            page = pageNumber,
            pageSize = pageSize,
            ordering = "-record__startAt",
        )
    }

    private fun getUserSearchParameters(authentication: CommonGroundAuthentication): List<ObjectSearchParameter> {
        return listOf(
            ObjectSearchParameter("identificatie__type", Comparator.EQUAL_TO, authentication.userType),
            ObjectSearchParameter("identificatie__value", Comparator.EQUAL_TO, authentication.userId),
        )
    }

    private fun isAuthorizedForTaak(
        authentication: CommonGroundAuthentication,
        taakIdentificatie: TaakIdentificatie,
    ): Boolean {
        return taakIdentificatie.type.lowercase() == authentication.userType && taakIdentificatie.value == authentication.userId
    }
}