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
import nl.nlportal.openklant.client.domain.HadKlantcontact
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdres
import nl.nlportal.openklant.client.domain.OpenKlant2Identificator
import nl.nlportal.openklant.client.domain.OpenKlant2IdentificeerdePartij
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificator
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificatorenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2UUID
import nl.nlportal.openklant.client.domain.PartijExpandOptions.DIGITALE_ADRESSEN
import nl.nlportal.openklant.client.domain.PartijExpandOptions.HAD_KLANTCONTACT
import nl.nlportal.openklant.client.domain.asSoortPartij
import nl.nlportal.openklant.client.path.DigitaleAdressen
import nl.nlportal.openklant.client.path.KlantContacten
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
                OpenKlant2PartijIdentificatorenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
            )

        try {
            return openKlant2Client.path<PartijIdentificatoren>().get(searchFilters)
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to find Partij Identificatoren: ${ex.responseBodyAsString}", ex)
            return null
        }
    }

    suspend fun findDigitaleAdressen(authentication: CommonGroundAuthentication): List<OpenKlant2DigitaleAdres>? {
        val searchVariables =
            listOf(
                OpenKlant2PartijenFilters.SOORT_PARTIJ to authentication.asSoortPartij(),
                OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
                OpenKlant2PartijenFilters.EXPAND to DIGITALE_ADRESSEN,
            )

        val response =
            try {
                openKlant2Client.path<Partijen>().get(searchVariables)?.singleOrNull()
            } catch (ex: WebClientResponseException) {
                logger.debug("Failed to get Partij with DigitaleAdressen: ${ex.responseBodyAsString}", ex)
                return null
            }

        return response?.expand?.digitaleAdressen
    }

    suspend fun createDigitaleAdres(
        authentication: CommonGroundAuthentication,
        digitaleAdres: OpenKlant2DigitaleAdres,
    ): OpenKlant2DigitaleAdres? {
        val userPartijId =
            findPartijIdentificatoren(authentication)
                ?.singleOrNull { it.partijIdentificator?.objectId == authentication.userId }
                ?.identificeerdePartij
                ?.uuid

        if (userPartijId == null) {
            logger.debug("Failed to create Digitale Adres: Authenticated User does not have a Partij")
            return null
        }

        val digitaleAdresResponse =
            try {
                openKlant2Client
                    .path<DigitaleAdressen>()
                    .create(
                        digitaleAdres
                            .copy(verstrektDoorPartij = OpenKlant2UUID(userPartijId)),
                    )
            } catch (ex: WebClientResponseException) {
                logger.debug("Failed to create DigitaleAdres: ${ex.responseBodyAsString}", ex)
                return null
            }

        return digitaleAdresResponse
    }

    suspend fun updateDigitaleAdresById(
        authentication: CommonGroundAuthentication,
        digitaleAdresId: UUID,
        digitaleAdres: OpenKlant2DigitaleAdres,
    ): OpenKlant2DigitaleAdres? {
        val previousDigitaleAdres = findDigitaleAdressen(authentication)?.singleOrNull { it.uuid == digitaleAdresId }
        if (previousDigitaleAdres == null) {
            logger.debug("Failed to update DigitaleAdres: No DigitaleAdres exists with provided Id")
            return null
        }
        val updatedDigitaleAdres =
            previousDigitaleAdres.copy(
                adres = digitaleAdres.adres,
                omschrijving = digitaleAdres.omschrijving,
                soortDigitaalAdres = digitaleAdres.soortDigitaalAdres,
                verstrektDoorBetrokkene = digitaleAdres.verstrektDoorBetrokkene,
                verstrektDoorPartij = digitaleAdres.verstrektDoorPartij,
            )
        val digitaleAdresResponse =
            try {
                openKlant2Client
                    .path<DigitaleAdressen>()
                    .put(updatedDigitaleAdres)
            } catch (ex: WebClientResponseException) {
                logger.debug("Failed to update DigitaleAdres: ${ex.responseBodyAsString}", ex)
                return null
            }

        return digitaleAdresResponse
    }

    suspend fun deleteDigitaleAdresById(
        authentication: CommonGroundAuthentication,
        digitaleAdresId: UUID,
    ) {
        val userPartijId =
            findPartijIdentificatoren(authentication)
                ?.singleOrNull { it.partijIdentificator?.objectId == authentication.userId }
                ?.identificeerdePartij
                ?.uuid

        if (userPartijId == null) {
            logger.debug("Failed to delete Digitale Adres: Given DigitaleAdres does not belong to Authenticated User")
            return
        }

        try {
            openKlant2Client
                .path<DigitaleAdressen>()
                .delete(digitaleAdresId)
        } catch (ex: WebClientResponseException) {
            logger.debug("Failed to delete DigitaleAdres: ${ex.responseBodyAsString}", ex)
            return
        }

        return
    }

    suspend fun findKlantContacten(authentication: CommonGroundAuthentication): List<HadKlantcontact>? {
        val searchVariables =
            listOf(
                OpenKlant2PartijenFilters.SOORT_PARTIJ to authentication.asSoortPartij(),
                OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
                OpenKlant2PartijenFilters.EXPAND to HAD_KLANTCONTACT,
            )

        val response =
            try {
                openKlant2Client.path<Partijen>().get(searchVariables)?.singleOrNull()
            } catch (ex: WebClientResponseException) {
                logger.debug("Failed to get Partij with Klantcontacten: ${ex.responseBodyAsString}", ex)
                return null
            }

        return response?.expand?.hadKlantcontact
    }

    suspend fun findKlantContact(klantContactId: UUID): HadKlantcontact? {
        return openKlant2Client
            .path<KlantContacten>()
            .get(klantContactId)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}