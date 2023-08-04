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
import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.commonground.authentication.exception.UserTypeUnsupportedException
import com.ritense.portal.core.util.Mapper
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.autoconfigure.ObjectsApiTaakConfig
import nl.nlportal.zgw.taak.domain.Taak
import nl.nlportal.zgw.taak.domain.TaakObject
import nl.nlportal.zgw.taak.domain.TaakStatus
import nl.nlportal.zgw.taak.graphql.TaakPage
import java.util.UUID

open class TaakService(
    private val objectsApiClient: ObjectsApiClient,
    private val objectsApiTaskConfig: ObjectsApiTaakConfig
) {

    suspend fun getTaken(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication
    ): TaakPage {
        val userSearchParameters = getUserSearchParameters(authentication)
        val statusOpenSearchParameter = ObjectSearchParameter("status", Comparator.EQUAL_TO, "open")

        return objectsApiClient.getObjects<TaakObject>(
            objectSearchParameters = userSearchParameters + statusOpenSearchParameter,
            objectTypeUrl = objectsApiTaskConfig.typeUrl,
            page = pageNumber,
            pageSize = pageSize,
            ordering = "-record__startAt"
        ).let { TaakPage.fromResultPage(pageNumber, pageSize, it) }
    }

    suspend fun getTaakById(id: UUID, authentication: CommonGroundAuthentication): Taak {
        return Taak.fromObjectsApiTask(getObjectsApiTaak(id, authentication))
    }

    suspend fun submitTaak(id: UUID, submission: ObjectNode, authentication: CommonGroundAuthentication): Taak {
        val objectsApiTask = getObjectsApiTaak(id, authentication)
        assert(objectsApiTask.record.data.status == TaakStatus.OPEN)
        val submissionAsMap = Mapper.get().convertValue(submission, object : TypeReference<Map<String, Any>>() {})

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
        updateRequest.record.data.verzondenData = submissionAsMap
        updateRequest.record.data.status = TaakStatus.INGEDIEND
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiTask.record.index.toString()

        val updatedObjectsApiTask = objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)
        return Taak.fromObjectsApiTask(updatedObjectsApiTask)
    }

    private suspend fun getObjectsApiTaak(
        taskId: UUID,
        authentication: CommonGroundAuthentication
    ): ObjectsApiObject<TaakObject> {
        val userSearchParameters = getUserSearchParameters(authentication)
        val taskIdSearchParameter = ObjectSearchParameter("verwerker_taak_id", Comparator.EQUAL_TO, taskId.toString())

        return objectsApiClient.getObjects<TaakObject>(
            objectSearchParameters = userSearchParameters + taskIdSearchParameter,
            objectTypeUrl = objectsApiTaskConfig.typeUrl,
            page = 1,
            pageSize = 2,
        ).results.single()
    }

    private fun getUserSearchParameters(authentication: CommonGroundAuthentication): List<ObjectSearchParameter> {
        return when (authentication) {
            is BurgerAuthentication -> {
                createIdentificatieSearchParameters("bsn", authentication.getBsn())
            }
            is BedrijfAuthentication -> {
                createIdentificatieSearchParameters("kvk", authentication.getKvkNummer())
            }
            else -> throw UserTypeUnsupportedException("User type not supported")
        }
    }

    private fun createIdentificatieSearchParameters(type: String, value: String): List<ObjectSearchParameter> {
        return listOf(
            ObjectSearchParameter("identificatie__type", Comparator.EQUAL_TO, type),
            ObjectSearchParameter("identificatie__value", Comparator.EQUAL_TO, value)
        )
    }
}