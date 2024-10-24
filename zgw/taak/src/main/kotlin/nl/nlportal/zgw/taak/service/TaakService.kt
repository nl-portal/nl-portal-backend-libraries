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
import nl.nlportal.zgw.taak.domain.TaakIdentificatie
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakStatus
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.graphql.TaakPageV2
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

open class TaakService(
    private val objectsApiClient: ObjectsApiClient,
    private val objectsApiTaskConfig: TaakObjectConfig,
) {
    suspend fun getTakenV2(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
        status: TaakStatus? = null,
        title: String? = null,
    ): TaakPageV2 {
        try {
            return getTakenResultPageV2<TaakObjectV2>(
                pageNumber,
                pageSize,
                authentication,
                zaakUUID,
                objectsApiTaskConfig.typeUrlV2,
                status,
                title,
            ).let { TaakPageV2.fromResultPage(pageNumber, pageSize, it) }
        } catch (ex: Exception) {
            return return TaakPageV2(
                number = pageNumber,
                size = pageSize,
                content = listOf(),
                totalElements = 0,
            )
        }
    }

    suspend fun getTaakByIdV2(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): TaakV2 {
        val taak =
            TaakV2.fromObjectsApi(
                getObjectsApiTaak<TaakObjectV2>(id),
            )
        // do validation if the user is authenticated for this task
        val isAuthorized = isAuthorizedForTaak(authentication, taak.identificatie)
        if (isAuthorized) {
            return taak
        }

        throw IllegalStateException("Access denied to this taak")
    }

    suspend fun submitTaakV2(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): TaakV2 {
        val objectsApiTask =
            getObjectsApiTaak<TaakObjectV2>(id)
        if (objectsApiTask.record.data.status != TaakStatus.OPEN) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("Status is niet open, taak [%s] kan niet afgerond worden", id),
            )
        }
        val submissionAsMap = Mapper.get().convertValue(submission, object : TypeReference<Map<String, Any>>() {})

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
        updateRequest.record.data.portaalformulier?.verzondenData = submissionAsMap
        updateRequest.record.data.status = TaakStatus.AFGEROND
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiTask.record.index.toString()

        val updatedObjectsApiTask = objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)
        return TaakV2.fromObjectsApi(updatedObjectsApiTask)
    }

    private suspend inline fun <reified T> getObjectsApiTaak(taskId: UUID): ObjectsApiObject<T> {
        val objectsApiTask = objectsApiClient.getObjectById<T>(taskId.toString())
        if (objectsApiTask == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("Taak kan niet gevonden worden", taskId),
            )
        }
        return objectsApiTask
    }

    private suspend inline fun <reified T> getTakenResultPageV2(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
        objectTypeUrl: String,
        status: TaakStatus?,
        title: String?,
    ): ResultPage<ObjectsApiObject<T>> {
        val objectSearchParameters = mutableListOf<ObjectSearchParameter>()

        objectSearchParameters.addAll(getUserSearchParameters(authentication))
        when {
            status != null -> {
                objectSearchParameters.add(ObjectSearchParameter("status", Comparator.EQUAL_TO, status.value))
            }
            else -> {
                objectSearchParameters.add(ObjectSearchParameter("status", Comparator.EQUAL_TO, TaakStatus.OPEN.value))
            }
        }

        zaakUUID?.let {
            objectSearchParameters.add(ObjectSearchParameter("koppeling__registratie", Comparator.EQUAL_TO, "zaak"))
            objectSearchParameters.add(
                ObjectSearchParameter(
                    "koppeling__uuid",
                    Comparator.STRING_CONTAINS,
                    it.toString(),
                ),
            )
        }

        title?.let {
            objectSearchParameters.add(
                ObjectSearchParameter(
                    "titel",
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