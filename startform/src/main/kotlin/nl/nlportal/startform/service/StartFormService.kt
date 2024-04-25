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
package nl.nlportal.startform.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.commonground.authentication.exception.UserTypeUnsupportedException
import nl.nlportal.startform.autoconfigure.StartFormConfig
import nl.nlportal.startform.domain.StartForm
import nl.nlportal.startform.domain.AanvragerIdentificatie
import nl.nlportal.startform.domain.StartFormDTO
import nl.nlportal.startform.domain.StartFormObject
import nl.nlportal.startform.repository.StartFormRepository
import nl.nlportal.zgw.objectenapi.domain.*
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class StartFormService(
    private val objectMapper: ObjectMapper,
    private val objectenApiService: ObjectenApiService,
    private val startFormConfig: StartFormConfig,
    private val startFormRepository: StartFormRepository,
) {
    suspend fun saveFormToObjectsApi(
        formName: String,
        submissionData: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): UUID {
        val startForm = getStartFormByFormName(formName)

        val startFormObject =
            StartFormObject(
                aanvragerIdentificatie(authentication),
                submissionData,
            )

        val startFormRecord =
            CreateObjectsApiObjectRequestRecordWithoutCorrection(
                typeVersion = startForm.typeVersion,
                data = objectMapper.valueToTree(startFormObject) as ObjectNode,
                startAt = localDateTimeNowObejctsApiFormat(),
            )

        val objectsApiObjectRequest =
            CreateObjectsApiObjectRequestWithoutCorrection(
                uuid = UUID.randomUUID(),
                type = "${startFormConfig.typeBaseUrl}/objecttypes/${startForm.typeUUID}",
                record = startFormRecord,
            )

        val createdObject = objectenApiService.createObjectWithoutCorrection(objectsApiObjectRequest)
        return createdObject.uuid
    }

    private fun localDateTimeNowObejctsApiFormat(): String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return now.format(formatter)
    }

    private fun aanvragerIdentificatie(authentication: CommonGroundAuthentication): AanvragerIdentificatie {
        return when (authentication) {
            is BurgerAuthentication -> {
                AanvragerIdentificatie("bsn", authentication.getBsn())
            }

            is BedrijfAuthentication -> {
                AanvragerIdentificatie("kvk", authentication.getKvkNummer())
            }

            else -> throw UserTypeUnsupportedException("User type not supported")
        }
    }

    suspend fun getStartFormByFormName(formName: String): StartForm {
        return withContext(Dispatchers.IO) {
            startFormRepository.findByFormName(formName)
        } ?: throw EntityNotFoundException("Cant find start form by $formName")
    }

    fun findStartFormByFormName(formName: String): StartForm? {
        return startFormRepository.findByFormName(formName)
    }

    fun createStartForm(startForm: StartForm): StartForm {
        return startFormRepository.save(startForm)
    }

    fun getAllStartFormDTOs(): List<StartFormDTO> {
        return startFormRepository.findAll().map {
            StartFormDTO(
                it.id,
                it.formName,
            )
        }
    }
}