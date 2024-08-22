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
import nl.nlportal.zgw.taak.domain.TaakVersion
import nl.nlportal.zgw.taak.graphql.TaakPage
import nl.nlportal.zgw.taak.graphql.TaakPageV2
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

open class TaakService(
    private val objectsApiClient: ObjectsApiClient,
    private val objectsApiTaskConfig: TaakObjectConfig,
) {
    @Deprecated("Use version 2, for migration only")
    suspend fun getTaken(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
    ): TaakPageV2 {
        val migratedList =
            getTakenV1(
                pageNumber,
                pageSize,
                authentication,
                zaakUUID,
            ).let {
                it.content.map {
                    TaakV2.migrate(it)
                }
            }

        val taakListV2 =
            getTakenV2(
                pageNumber,
                pageSize,
                authentication,
                zaakUUID,
            ).content + migratedList

        return TaakPageV2(
            pageNumber,
            pageSize,
            taakListV2,
            taakListV2.size,
        )
    }

    @Deprecated("Use version 2")
    suspend fun getTakenV1(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
    ): TaakPage {
        try {
            return getTakenResultPage<TaakObject>(
                pageNumber,
                pageSize,
                authentication,
                zaakUUID,
                objectsApiTaskConfig.typeUrl,
            ).let { TaakPage.fromResultPage(pageNumber, pageSize, it) }
        } catch (ex: Exception) {
            return return TaakPage(
                number = pageNumber,
                size = pageSize,
                content = listOf(),
                totalElements = 0,
            )
        }
    }

    suspend fun getTakenV2(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication,
        zaakUUID: UUID? = null,
    ): TaakPageV2 {
        try {
            return getTakenResultPageV2<TaakObjectV2>(
                pageNumber,
                pageSize,
                authentication,
                zaakUUID,
                objectsApiTaskConfig.typeUrlV2,
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

    @Deprecated("Use version 2")
    suspend fun getTaakById(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): TaakV2 {
        return try {
            TaakV2.migrate(
                getTaakByIdV1(
                    id,
                    authentication,
                ),
            )
        } catch (ex: Exception) {
            getTaakByIdV2(
                id,
                authentication,
            )
        }
    }

    @Deprecated("Use version 2")
    suspend fun getTaakByIdV1(
        id: UUID,
        authentication: CommonGroundAuthentication,
    ): Taak {
        val taak =
            Taak.fromObjectsApiTask(
                getObjectsApiTaak<TaakObject>(id),
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
                getObjectsApiTaak<TaakObjectV2>(id),
            )
        // do validation if the user is authenticated for this task
        val isAuthorized = isAuthorizedForTaak(authentication, taak.identificatie)
        if (isAuthorized) {
            return taak
        }

        throw IllegalStateException("Access denied to this taak")
    }

    suspend fun submitTaak(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
        version: TaakVersion,
    ): TaakV2 {
        val submittedTask =
            when (version) {
                TaakVersion.V1 ->
                    TaakV2.migrate(
                        submitTaakV1(
                            id,
                            submission,
                            authentication,
                        ),
                    )
                else ->
                    submitTaakV2(
                        id,
                        submission,
                        authentication,
                    )
            }

        return submittedTask
    }

    @Deprecated("Use version 2")
    suspend fun submitTaakV1(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): Taak {
        val objectsApiTask =
            getObjectsApiTaak<TaakObject>(id)
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
            getObjectsApiTaak<TaakObjectV2>(id)
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

    private suspend inline fun <reified T> getTakenResultPageV2(
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
            objectSearchParameters.add(ObjectSearchParameter("koppeling__registratie", Comparator.EQUAL_TO, "zaak"))
            objectSearchParameters.add(
                ObjectSearchParameter(
                    "koppeling__uuid",
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