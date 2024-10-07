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

import mu.KotlinLogging
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import nl.nlportal.openklant.client.domain.OpenKlant2Identificator
import nl.nlportal.openklant.client.domain.OpenKlant2IdentificeerdePartij
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificator
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificatorenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificatorenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters
import nl.nlportal.openklant.client.domain.asSoortPartij
import nl.nlportal.openklant.client.path.PartijIdentificatoren
import nl.nlportal.openklant.client.path.Partijen
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.UUID

class OpenKlant2Service(
    private val openKlant2Client: OpenKlant2KlantinteractiesClient,
) {
    suspend fun findPartijByAuthentication(authentication: CommonGroundAuthentication): OpenKlant2Partij? {
        val searchVariables =
            listOf(
                OpenKlant2PartijenFilters.SOORT_PARTIJ to authentication.asSoortPartij(),
                OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
            )

        try {
            return openKlant2Client.path<Partijen>().get(searchVariables)?.singleOrNull()
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
        partij: OpenKlant2Partij,
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
                openKlant2Client.path<Partijen>().create(partij)
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
    ): OpenKlant2Partij? {
        val previousPartij = findPartijByAuthentication(authentication)
        if (previousPartij != null) {
            val updatedPartij =
                previousPartij.copy(
                    indicatieGeheimhouding = partij.indicatieGeheimhouding,
                    indicatieActief = partij.indicatieActief,
                    soortPartij = partij.soortPartij,
                    partijIdentificatie = partij.partijIdentificatie,
                )
            val partijResponse =
                try {
                    openKlant2Client
                        .path<Partijen>()
                        .put(updatedPartij)
                } catch (ex: WebClientResponseException) {
                    logger.debug("Failed to update Partij: ${ex.responseBodyAsString}", ex)
                    return null
                }

            return partijResponse
        }

        logger.debug("Failed to update Partij: No existing Partij found. Creating new Partij")
        return createPartijWithIdentificator(authentication, partij)
    }

    suspend fun findPartijIdentificatoren(authentication: CommonGroundAuthentication): List<OpenKlant2PartijIdentificator>? {
        val searchFilters: List<Pair<OpenKlant2PartijIdentificatorenFilters, String>> =
            listOf(
                PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
            )

        try {
            return openKlant2Client.path<PartijIdentificatoren>().find(searchFilters)
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to find Partij Identificatoren: ${ex.responseBodyAsString}", ex)
            return null
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}