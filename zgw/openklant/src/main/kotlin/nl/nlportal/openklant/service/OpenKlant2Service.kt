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

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration.OpenKlantConfigurationProperties
import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import nl.nlportal.openklant.client.domain.Contactnaam
import nl.nlportal.openklant.client.domain.ContactpersoonIdentificatie
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdres
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdresUpdate
import nl.nlportal.openklant.client.domain.OpenKlant2DigitaleAdressenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2Identificator
import nl.nlportal.openklant.client.domain.OpenKlant2IdentificeerdePartij
import nl.nlportal.openklant.client.domain.OpenKlant2Klantcontact
import nl.nlportal.openklant.client.domain.OpenKlant2KlantcontactenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2Partij
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificator
import nl.nlportal.openklant.client.domain.OpenKlant2PartijIdentificatorenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2PartijenFilters
import nl.nlportal.openklant.client.domain.OpenKlant2SubIdentificatorVan
import nl.nlportal.openklant.client.domain.OpenKlant2UUID
import nl.nlportal.openklant.client.domain.OrganisatieIdentificatie
import nl.nlportal.openklant.client.domain.PartijIdentificatie
import nl.nlportal.openklant.client.domain.PartijIdentificatorCodeRegister
import nl.nlportal.openklant.client.domain.PartijIdentificatorCodeSoort
import nl.nlportal.openklant.client.domain.PartijIdentificatorCodeType
import nl.nlportal.openklant.client.domain.PersoonsIdentificatie
import nl.nlportal.openklant.client.domain.asSoortPartij
import nl.nlportal.openklant.client.domain.asSoortPartijEnum
import nl.nlportal.openklant.client.path.DigitaleAdressen
import nl.nlportal.openklant.client.path.KlantContacten
import nl.nlportal.openklant.client.path.PartijIdentificatoren
import nl.nlportal.openklant.client.path.Partijen
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.UUID

class OpenKlant2Service(
    private val openKlant2Client: OpenKlant2KlantinteractiesClient,
    private val openKlantConfigurationProperties: OpenKlantConfigurationProperties,
) {
    suspend fun findPartijByAuthentication(authentication: CommonGroundAuthentication): OpenKlant2Partij? {
        val searchVariables = searchVariablesPartij(authentication)

        try {
            return openKlant2Client.path<Partijen>().get(searchVariables)?.singleOrNull()
        } catch (ex: WebClientResponseException) {
            logger.warn(ex) { "Failed to find Partij: ${ex.responseBodyAsString}" }
            return null
        }
    }

    suspend fun getPartij(partijId: UUID): OpenKlant2Partij? {
        try {
            return openKlant2Client.path<Partijen>().get(partijId)
        } catch (ex: WebClientResponseException) {
            logger.error(ex) { "Failed to find Partij: ${ex.responseBodyAsString}" }
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
                    createPartijIndicator(authentication),
            )
        val partijResponse =
            try {
                openKlant2Client
                    .path<Partijen>()
                    .create(partij)
                    .also {
                        try {
                            openKlant2Client
                                .path<PartijIdentificatoren>()
                                .create(
                                    partijIdentificator
                                        .copy(
                                            identificeerdePartij = OpenKlant2IdentificeerdePartij(it.uuid!!),
                                        ),
                                ).also {
                                    val vestigingsNummer = authentication.getVestigingsNummer()
                                    if (vestigingsNummer != null) {
                                        openKlant2Client
                                            .path<PartijIdentificatoren>()
                                            .create(
                                                OpenKlant2PartijIdentificator(
                                                    identificeerdePartij = OpenKlant2IdentificeerdePartij(it.identificeerdePartij?.uuid!!),
                                                    subIdentificatorVan = OpenKlant2SubIdentificatorVan(it.uuid!!),
                                                    partijIdentificator =
                                                        OpenKlant2Identificator(
                                                            objectId = vestigingsNummer,
                                                            codeSoortObjectId = PartijIdentificatorCodeSoort.VESTIGINGSNUMMER.soort,
                                                            codeObjecttype = PartijIdentificatorCodeType.VESTIGING.type,
                                                            codeRegister = PartijIdentificatorCodeRegister.HR.register,
                                                        ),
                                                ),
                                            )
                                    }
                                }
                        } catch (ex: WebClientResponseException) {
                            logger.error(ex) { "Failed to create PartijIdentificator" }
                            openKlant2Client.path<Partijen>().delete(it.uuid!!)
                            throw ex
                        }
                    }
            } catch (ex: WebClientResponseException) {
                logger.error(ex) { "Failed to create Partij: ${ex.responseBodyAsString}" }
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
                    logger.error(ex) { "Failed to update Partij: ${ex.responseBodyAsString}" }
                    return null
                }

            return partijResponse
        }

        logger.warn { "Failed to update Partij: No existing Partij found. Creating new Partij" }
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
            logger.error(ex) { "Failed to find Partij Identificatoren: ${ex.responseBodyAsString}" }
            return null
        }
    }

    suspend fun findDigitaleAdressen(authentication: CommonGroundAuthentication): List<OpenKlant2DigitaleAdres> {
        val searchVariables = searchVariablesDigitaleAdressen(authentication).toMutableList()

        openKlantConfigurationProperties.digitalAdressenReferentie?.let {
            searchVariables.add(OpenKlant2DigitaleAdressenFilters.REFERENTIE to it)
        }

        val response =
            try {
                openKlant2Client.path<DigitaleAdressen>().get(searchVariables)
            } catch (ex: WebClientResponseException) {
                logger.error(ex) { "Failed to get Partij with DigitaleAdressen: ${ex.responseBodyAsString}" }
                return emptyList()
            }

        return response
    }

    suspend fun createDigitaleAdres(
        authentication: CommonGroundAuthentication,
        digitaleAdres: OpenKlant2DigitaleAdres,
    ): OpenKlant2DigitaleAdres? {
        var userPartijId =
            findPartijIdentificatoren(authentication)
                ?.singleOrNull { it.partijIdentificator?.objectId == authentication.userId }
                ?.identificeerdePartij
                ?.uuid

        if (userPartijId == null) {
            logger.debug { "Authenticated User does not have a Partij, let's create a new partij" }
            val partij =
                createPartijWithIdentificator(
                    authentication = authentication,
                    partij =
                        OpenKlant2Partij(
                            indicatieActief = true,
                            soortPartij = authentication.asSoortPartijEnum(),
                            partijIdentificatie =
                                createPartijIdentificatie(
                                    authentication = authentication,
                                ),
                        ),
                )

            if (partij == null) {
                logger.debug { "Creating new partij failed for digitale adressen" }
                return null
            }

            userPartijId = partij.uuid
        }

        val digitaleAdresResponse =
            try {
                openKlant2Client
                    .path<DigitaleAdressen>()
                    .create(
                        digitaleAdres
                            .copy(
                                verstrektDoorPartij = OpenKlant2UUID(userPartijId!!),
                                referentie = openKlantConfigurationProperties.digitalAdressenReferentie ?: "",
                            ),
                    )
            } catch (ex: WebClientResponseException) {
                logger.error(ex) { "Failed to create DigitaleAdres: ${ex.responseBodyAsString}" }
                return null
            }

        return digitaleAdresResponse
    }

    suspend fun updateDigitaleAdresById(
        authentication: CommonGroundAuthentication,
        digitaleAdres: OpenKlant2DigitaleAdresUpdate,
    ): OpenKlant2DigitaleAdres? {
        try {
            val previousDigitaleAdres = findDigitaleAdressen(authentication).singleOrNull { it.uuid == digitaleAdres.uuid }
            if (previousDigitaleAdres == null) {
                logger.debug { "Failed to update DigitaleAdres: No DigitaleAdres exists with provided Id" }
                return null
            }
            val digitaleAdresResponse =
                try {
                    openKlant2Client
                        .path<DigitaleAdressen>()
                        .patch(digitaleAdres = digitaleAdres)
                } catch (ex: WebClientResponseException) {
                    logger.debug(ex) { "Failed to update DigitaleAdres: ${ex.responseBodyAsString}" }
                    return null
                }

            return digitaleAdresResponse
        } catch (ex: WebClientResponseException) {
            logger.error(ex) { "Failed to update DigitaleAdres for uuid - ${digitaleAdres.uuid}: ${ex.responseBodyAsString}" }
            return null
        }
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
            logger.debug { "Failed to delete Digitale Adres: Given DigitaleAdres does not belong to Authenticated User" }
            return
        }

        try {
            openKlant2Client
                .path<DigitaleAdressen>()
                .delete(digitaleAdresId)
        } catch (ex: WebClientResponseException) {
            logger.error(ex) { "Failed to delete DigitaleAdres: ${ex.responseBodyAsString}" }
            return
        }

        return
    }

    suspend fun findKlantContacten(authentication: CommonGroundAuthentication): List<OpenKlant2Klantcontact> {
        val searchVariables = searchVariablesKlantcontacten(authentication)

        return try {
            openKlant2Client.path<KlantContacten>().get(searchVariables)
        } catch (ex: WebClientResponseException) {
            logger.error(ex) { "Failed to get Partij with Klantcontacten: ${ex.responseBodyAsString}" }
            return emptyList()
        }
    }

    suspend fun findKlantContact(klantContactId: UUID): OpenKlant2Klantcontact? =
        openKlant2Client
            .path<KlantContacten>()
            .get(klantContactId)

    private fun createPartijIndicator(authentication: CommonGroundAuthentication): OpenKlant2Identificator =
        when (authentication) {
            is BurgerAuthentication -> {
                OpenKlant2Identificator(
                    objectId = authentication.userId,
                    codeSoortObjectId = PartijIdentificatorCodeSoort.BSN.soort,
                    codeObjecttype = PartijIdentificatorCodeType.NATUURLIJKPERSOON.type,
                    codeRegister = PartijIdentificatorCodeRegister.BRP.register,
                )
            }
            is BedrijfAuthentication -> {
                OpenKlant2Identificator(
                    objectId = authentication.userId,
                    codeSoortObjectId = PartijIdentificatorCodeSoort.KVKNUMMER.soort,
                    codeObjecttype = PartijIdentificatorCodeType.NIETNATUURLIJKPERSOON.type,
                    codeRegister = PartijIdentificatorCodeRegister.HR.register,
                )
            }
            else -> throw IllegalArgumentException("Unsupported authentication type: ${authentication::class.qualifiedName}")
        }

    private fun searchVariablesPartij(authentication: CommonGroundAuthentication): List<Pair<OpenKlant2PartijenFilters, String>> =
        when (authentication) {
            is BurgerAuthentication -> {
                listOf(
                    OpenKlant2PartijenFilters.SOORT_PARTIJ to authentication.asSoortPartij(),
                    OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
                )
            }

            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    listOf(
                        OpenKlant2PartijenFilters.SOORT_PARTIJ to authentication.asSoortPartij(),
                        OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to vestigingsNummer,
                    )
                } else {
                    listOf(
                        OpenKlant2PartijenFilters.SOORT_PARTIJ to authentication.asSoortPartij(),
                        OpenKlant2PartijenFilters.PARTIJ_IDENTIFICATOR_OBJECT_ID to authentication.userId,
                    )
                }
            }

            else -> throw IllegalArgumentException("Unsupported authentication type: ${authentication::class.qualifiedName}")
        }

    private fun searchVariablesDigitaleAdressen(
        authentication: CommonGroundAuthentication,
    ): List<Pair<OpenKlant2DigitaleAdressenFilters, Any>> =
        when (authentication) {
            is BurgerAuthentication -> {
                listOf(
                    OpenKlant2DigitaleAdressenFilters.PAGE to 1,
                    OpenKlant2DigitaleAdressenFilters.VERSTREKTDOORPARTIJ_PARTIJ_IDENTIFICATOR_CODE_OBJECTID to authentication.userId,
                )
            }

            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    listOf(
                        OpenKlant2DigitaleAdressenFilters.PAGE to 1,
                        OpenKlant2DigitaleAdressenFilters.VERSTREKTDOORPARTIJ_PARTIJ_IDENTIFICATOR_CODE_OBJECTID to vestigingsNummer,
                    )
                } else {
                    listOf(
                        OpenKlant2DigitaleAdressenFilters.PAGE to 1,
                        OpenKlant2DigitaleAdressenFilters.VERSTREKTDOORPARTIJ_PARTIJ_IDENTIFICATOR_CODE_OBJECTID to authentication.userId,
                    )
                }
            }

            else -> throw IllegalArgumentException("Unsupported authentication type: ${authentication::class.qualifiedName}")
        }

    private fun searchVariablesKlantcontacten(
        authentication: CommonGroundAuthentication,
    ): List<Pair<OpenKlant2KlantcontactenFilters, Any>> =
        when (authentication) {
            is BurgerAuthentication -> {
                listOf(
                    OpenKlant2KlantcontactenFilters.PAGE to 1,
                    OpenKlant2KlantcontactenFilters.HADBETROKKENE_PARTIJ_IDENTIFICATOR_CODE_OBJECTID to authentication.userId,
                )
            }

            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    listOf(
                        OpenKlant2KlantcontactenFilters.PAGE to 1,
                        OpenKlant2KlantcontactenFilters.HADBETROKKENE_PARTIJ_IDENTIFICATOR_CODE_OBJECTID to vestigingsNummer,
                    )
                } else {
                    listOf(
                        OpenKlant2KlantcontactenFilters.PAGE to 1,
                        OpenKlant2KlantcontactenFilters.HADBETROKKENE_PARTIJ_IDENTIFICATOR_CODE_OBJECTID to authentication.userId,
                    )
                }
            }

            else -> throw IllegalArgumentException("Unsupported authentication type: ${authentication::class.qualifiedName}")
        }

    private fun createPartijIdentificatie(authentication: CommonGroundAuthentication): PartijIdentificatie =
        when (authentication) {
            is BurgerAuthentication -> {
                PersoonsIdentificatie(
                    contactnaam = Contactnaam(),
                )
            }

            is BedrijfAuthentication -> {
                OrganisatieIdentificatie(
                    naam = authentication.userId,
                )
            }

            else ->
                ContactpersoonIdentificatie(
                    uuid = UUID.randomUUID(),
                )
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}