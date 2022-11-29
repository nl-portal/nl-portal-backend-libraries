/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.gzac.objectsapi.task.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.commonground.authentication.exception.UserTypeUnsupportedException
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.gzac.objectsapi.client.ObjectsApiClient
import com.ritense.portal.gzac.objectsapi.domain.Comparator
import com.ritense.portal.gzac.objectsapi.domain.ObjectSearchParameter
import com.ritense.portal.gzac.objectsapi.domain.ObjectsApiObject
import com.ritense.portal.gzac.objectsapi.domain.UpdateObjectsApiObjectRequest
import com.ritense.portal.gzac.objectsapi.task.autoconfiguration.ObjectsApiTaskConfig
import com.ritense.portal.gzac.objectsapi.task.domain.ObjectsApiTask
import com.ritense.portal.gzac.objectsapi.task.domain.Task
import com.ritense.portal.gzac.objectsapi.task.domain.TaskStatus
import com.ritense.portal.gzac.objectsapi.task.graphql.TaskPage
import java.util.UUID

open class TaskService(
    private val objectsApiClient: ObjectsApiClient,
    private val objectsApiTaskConfig: ObjectsApiTaskConfig
) {

    suspend fun getTasks(
        pageNumber: Int,
        pageSize: Int,
        authentication: CommonGroundAuthentication
    ): TaskPage {
        val userSearchParameter = getUserSearchParameter(authentication)
        val statusOpenSearchParameter = ObjectSearchParameter("status", Comparator.EQUAL_TO, "open")

        return objectsApiClient.getObjects<ObjectsApiTask>(
            objectSearchParameters = listOf(userSearchParameter, statusOpenSearchParameter),
            objectTypeUrl = objectsApiTaskConfig.typeUrl,
            page = pageNumber,
            pageSize = pageSize,
            ordering = "-record__startAt"
        ).let { TaskPage.fromResultPage(pageNumber, pageSize, it) }
    }

    suspend fun getTaskById(id: UUID, authentication: CommonGroundAuthentication): Task {
        return Task.fromObjectsApiTask(getObjectsApiTask(id, authentication))
    }

    suspend fun submitTask(id: UUID, submission: ObjectNode, authentication: CommonGroundAuthentication): Task {
        val objectsApiTask = getObjectsApiTask(id, authentication)
        assert(objectsApiTask.record.data.status == TaskStatus.OPEN)
        val submissionAsMap = Mapper.get().convertValue(submission, object : TypeReference<Map<String, Any>>() {})

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiTask)
        updateRequest.record.data.verzondenData = submissionAsMap
        updateRequest.record.data.status = TaskStatus.INGEDIEND
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiTask.record.index.toString()

        val updatedObjectsApiTask = objectsApiClient.updateObject(objectsApiTask.uuid, updateRequest)
        return Task.fromObjectsApiTask(updatedObjectsApiTask)
    }

    private suspend fun getObjectsApiTask(
        taskId: UUID,
        authentication: CommonGroundAuthentication
    ): ObjectsApiObject<ObjectsApiTask> {
        val userSearchParameter = getUserSearchParameter(authentication)
        val taskIdSearchParameter = ObjectSearchParameter("verwerker_taak_id", Comparator.EQUAL_TO, taskId.toString())

        return objectsApiClient.getObjects<ObjectsApiTask>(
            objectSearchParameters = listOf(userSearchParameter, taskIdSearchParameter),
            objectTypeUrl = objectsApiTaskConfig.typeUrl,
            page = 1,
            pageSize = 2,
        ).results.single()
    }

    private fun getUserSearchParameter(authentication: CommonGroundAuthentication): ObjectSearchParameter {
        return when (authentication) {
            is BurgerAuthentication -> ObjectSearchParameter("bsn", Comparator.EQUAL_TO, authentication.getBsn())
            is BedrijfAuthentication -> ObjectSearchParameter("kvk", Comparator.EQUAL_TO, authentication.getKvkNummer())
            else -> throw UserTypeUnsupportedException("User type not supported")
        }
    }
}