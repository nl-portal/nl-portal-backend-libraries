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
package nl.nlportal.haalcentraal2.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.haalcentraal2.autoconfiguration.HaalCentraal2ModuleConfiguration.HaalCentraal2ConfigurationProperties
import nl.nlportal.haalcentraal2.client.HaalCentraal2BewoningClient
import nl.nlportal.haalcentraal2.client.HaalCentraal2BrpClient
import nl.nlportal.haalcentraal2.client.path.Bewoningen
import nl.nlportal.haalcentraal2.client.path.Personen
import nl.nlportal.haalcentraal2.domain.bewoning.BewoningenApiRequest
import nl.nlportal.haalcentraal2.domain.bewoning.BewoningenApiResponse
import nl.nlportal.haalcentraal2.domain.brp.AanduidingNaamGebruikBrpNaam
import nl.nlportal.haalcentraal2.domain.brp.BrpApiRequest
import nl.nlportal.haalcentraal2.domain.brp.BrpPersoon
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HaalCentraal2Service(
    val haalCentraal2ConfigurationProperties: HaalCentraal2ConfigurationProperties,
    val haalCentraal2BrpClient: HaalCentraal2BrpClient,
    val haalCentraal2BewoningClient: HaalCentraal2BewoningClient,
) {
    suspend fun getPersoon(authentication: CommonGroundAuthentication): BrpPersoon? =
        if (authentication is BurgerAuthentication) {
            val brpApiRequest =
                BrpApiRequest(
                    burgerservicenummer = listOf(authentication.userId),
                    fields = haalCentraal2ConfigurationProperties.brpFields,
                )
            haalCentraal2BrpClient
                .path<Personen>()
                .post(
                    brpApiRequest,
                    authentication,
                ).personen
                .singleOrNull()
                ?.apply {
                    naam?.officialLastName = determineOfficialLastName(this)
                }
        } else {
            null
        }

    suspend fun getBewoningen(
        authentication: CommonGroundAuthentication,
        adresseerbaarObjectIdentificatie: String,
    ): BewoningenApiResponse? =
        try {
            if (authentication is BurgerAuthentication) {
                val bewoningenApiRequest =
                    BewoningenApiRequest(
                        type = "BewoningMetPeildatum",
                        peildatum = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        adresseerbaarObjectIdentificatie = adresseerbaarObjectIdentificatie,
                    )
                haalCentraal2BewoningClient.path<Bewoningen>().post(bewoningenApiRequest, authentication)
            } else {
                logger.warn { "Authentication is not supported" }
                null
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Something went wrong with 'getBewoningen' - error: ${ex.message}" }
            null
        }

    suspend fun getBewonersAantal(
        authentication: CommonGroundAuthentication,
        adresseerbaarObjectIdentificatie: String,
    ): Int? {
        getBewoningen(authentication, adresseerbaarObjectIdentificatie)?.bewoningen?.first {
            return it.bewoners.size
        }

        return null
    }

    suspend fun getGemachtigde(authentication: CommonGroundAuthentication): BrpPersoon? {
        val authenticationGemachtigde = authentication.getGemachtigde()

        return authenticationGemachtigde?.bsn?.let {
            val brpApiRequest =
                BrpApiRequest(
                    burgerservicenummer = listOf(it),
                    fields = haalCentraal2ConfigurationProperties.brpFields,
                )
            haalCentraal2BrpClient
                .path<Personen>()
                .post(
                    brpApiRequest,
                    authentication,
                ).personen
                .singleOrNull()
                ?.apply {
                    naam?.officialLastName = determineOfficialLastName(this)
                }
        }
    }

    private fun determineOfficialLastName(persoon: BrpPersoon): String {
        if (persoon.naam == null) {
            return ""
        }
        if (persoon.naam.aanduidingNaamgebruik?.code == null) {
            return persoon.naam.lastName()
        }
        val lastNamePartner =
            persoon.partners
                ?.firstOrNull { it.naam != null }
                ?.naam
                ?.lastName()
                ?.trim() ?: ""

        return when (persoon.naam.aanduidingNaamgebruik.code) {
            AanduidingNaamGebruikBrpNaam.PARTNER.value -> {
                lastNamePartner
            }

            AanduidingNaamGebruikBrpNaam.PARTNER_EIGEN.value -> {
                "$lastNamePartner - ${persoon.naam.lastName()}"
            }

            AanduidingNaamGebruikBrpNaam.EIGEN_PARTNER.value -> {
                "${persoon.naam.lastName()} - $lastNamePartner"
            }

            else -> {
                persoon.naam.lastName()
            }
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}