/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.service

import mu.KotlinLogging
import nl.nlportal.berichten.autoconfigure.BerichtenConfigurationProperties
import nl.nlportal.berichten.domain.Bericht
import nl.nlportal.berichten.graphql.BerichtenPage
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.zgw.objectenapi.domain.Comparator.EQUAL_TO
import nl.nlportal.zgw.objectenapi.domain.Comparator.STRING_CONTAINS
import nl.nlportal.zgw.objectenapi.domain.Comparator.LOWER_THAN_OR_EQUAL_TO
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.UUID

class BerichtenService(
    private val objectenApiService: ObjectenApiService,
    private val berichtenConfigurationProperties: BerichtenConfigurationProperties,
) {
    suspend fun getUnopenedBerichtenCount(authentication: CommonGroundAuthentication): Int {
        val searchParameters =
            listOf(
                ObjectSearchParameter("geopend", STRING_CONTAINS, "false"),
                ObjectSearchParameter("identificatie__type", EQUAL_TO, authentication.userType),
                ObjectSearchParameter("identificatie__value", EQUAL_TO, authentication.userId),
            )
        val results = getBerichten(1, 1, searchParameters)

        return results.count
    }

    suspend fun getBericht(
        authentication: CommonGroundAuthentication,
        id: UUID,
    ): Bericht? {
        val objectsApiBericht = objectenApiService.getObjectById<Bericht>(id.toString())
        if (objectsApiBericht == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Bericht not found")
        }

        val bericht = objectsApiBericht.record.data

        if (bericht.identificatie.value != authentication.userId) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized for this Bericht")
        }

        if (bericht.geopend) {
            // Bericht is already read, so return
            return bericht
        }

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiBericht)
        updateRequest.record.data.geopend = true
        updateRequest.record.correctedBy = authentication.userId
        updateRequest.record.correctionFor = objectsApiBericht.record.index.toString()
        val updatedObjectsApiBericht = objectenApiService.updateObject(objectsApiBericht.uuid, updateRequest)

        return updatedObjectsApiBericht?.record?.data
    }

    suspend fun getBerichtenPage(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
    ): BerichtenPage {
        val searchParameters =
            listOf(
                ObjectSearchParameter("identificatie__type", EQUAL_TO, authentication.userType),
                ObjectSearchParameter("identificatie__value", EQUAL_TO, authentication.userId),
                ObjectSearchParameter("publicatiedatum", LOWER_THAN_OR_EQUAL_TO, LocalDate.now().toString()),
            )
        val results = getBerichten(pageNumber, pageSize, searchParameters)

        return results.toBerichtenPage()
    }

    private suspend fun getBerichten(
        pageNumber: Int,
        pageSize: Int,
        searchParameters: List<ObjectSearchParameter> = emptyList(),
    ) = objectenApiService
        .getObjects<Bericht>(
            objectSearchParameters = searchParameters,
            objectTypeUrl = berichtenConfigurationProperties.berichtObjectTypeUrl,
            pageNumber = pageNumber,
            pageSize = pageSize,
        )

    private fun ResultPage<ObjectsApiObject<Bericht>>.toBerichtenPage(
        pageNumber: Int = 1,
        pageSize: Int = 20,
    ): BerichtenPage {
        val berichten = results.map { it.record.data.copy(id = it.uuid) }.sortedByDescending { it.publicatiedatum }

        return BerichtenPage(
            number = pageNumber,
            size = pageSize,
            content = berichten,
            totalElements = count,
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}