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
package nl.nlportal.openklant.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KotlinLogging
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.client.OpenKlant2Client
import nl.nlportal.openklant.client.path.Partijen
import nl.nlportal.openklant.domain.Partij
import nl.nlportal.openklant.domain.asSoortPartij
import org.springframework.util.MultiValueMapAdapter
import org.springframework.web.reactive.function.client.WebClientResponseException

class OpenKlant2Service(
    private val openKlant2Client: OpenKlant2Client,
) {
    suspend fun findPartij(authentication: CommonGroundAuthentication): Partij? {
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

    suspend fun createPartij(
        authentication: CommonGroundAuthentication,
        partij: ObjectNode,
    ): Partij? {
        try {
            return openKlant2Client.path<Partijen>().create(objectMapper.convertValue(partij))
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to create Partij: ${ex.responseBodyAsString}", ex)
            return null
        }
    }

    suspend fun updatePartij(
        authentication: CommonGroundAuthentication,
        partij: ObjectNode,
    ): Partij {
        return objectMapper.convertValue(partij)
    }

    companion object {
        private val objectMapper = Mapper.get()
        private val logger = KotlinLogging.logger {}
    }
}