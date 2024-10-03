/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.service

import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KotlinLogging
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.client.OpenKlant2Client
import nl.nlportal.openklant.client.domain.OpenKlant2Identificator
import nl.nlportal.openklant.client.domain.OpenKlant2IdentificeerdePartij
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificator
import nl.nlportal.openklant.client.domain.asSoortPartij
import nl.nlportal.openklant.client.path.PartijIdentificatoren
import nl.nlportal.openklant.client.path.Partijen
import org.springframework.util.MultiValueMapAdapter
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.UUID

class OpenKlant2Service(
    private val openKlant2Client: OpenKlant2Client,
) {
    suspend fun findPartij(authentication: CommonGroundAuthentication): OpenKlant2Partij? {
        val searchVariables =
            MultiValueMapAdapter(
                mapOf(
                    "soortPartij" to listOf(authentication.asSoortPartij()),
                    "partijIdentificator__objectId" to listOf(authentication.userId),
                ),
            )

        try {
            return openKlant2Client.path<Partijen>().find(searchVariables)
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to find Partij: ${ex.responseBodyAsString}", ex)
            return null
        }
    }

    suspend fun getPartij(partijId: UUID): OpenKlant2Partij? {
        try {
            return openKlant2Client.path<Partijen>().get(partijId)
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to find Partij: ${ex.responseBodyAsString}", ex)
            return null
        }
    }

    suspend fun createPartijWithIdentificator(
        authentication: CommonGroundAuthentication,
        partijRequest: OpenKlant2Partij,
    ): OpenKlant2Partij? {
        val partijIdentificator =
            OpenKlant2PartijIdentificator(
                partijIdentificator =
                    OpenKlant2Identificator(
                        objectId = authentication.userId,
                    ),
            )
        val partijResponse =
            try {
                openKlant2Client.path<Partijen>().create(partijRequest)
                    .also {
                        try {
                            openKlant2Client
                                .path<PartijIdentificatoren>()
                                .create(
                                    partijIdentificator
                                        .copy(
                                            identificeerdePartij = OpenKlant2IdentificeerdePartij(it.uuid!!),
                                        ),
                                )
                        } catch (ex: WebClientResponseException) {
                            logger.debug("Failed to create PartijIdentificator")
                            openKlant2Client.path<Partijen>().delete(it.uuid!!)
                            throw ex
                        }
                    }
            } catch (ex: WebClientResponseException) {
                logger.debug("Failed to create Partij: ${ex.responseBodyAsString}", ex)
                return null
            }

        return partijResponse
    }

    suspend fun updatePartij(
        authentication: CommonGroundAuthentication,
        partij: OpenKlant2Partij,
    ): OpenKlant2Partij {
        return objectMapper.convertValue(partij)
    }

    suspend fun findPartijIdentificatoren(authentication: CommonGroundAuthentication): List<OpenKlant2PartijIdentificator>? {
        val searchVariables =
            MultiValueMapAdapter(
                mapOf(
                    "partij_identificator_object_id" to listOf(authentication.userId),
                ),
            )

        try {
            return openKlant2Client.path<PartijIdentificatoren>().find(searchVariables)
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to find Partij Identificatoren: ${ex.responseBodyAsString}", ex)
            return null
        }
    }

    companion object {
        private val objectMapper = Mapper.get()
        private val logger = KotlinLogging.logger {}
    }
}