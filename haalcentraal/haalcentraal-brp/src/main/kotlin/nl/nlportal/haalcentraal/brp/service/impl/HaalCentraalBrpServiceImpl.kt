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
package nl.nlportal.haalcentraal.brp.service.impl

import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.haalcentraal.brp.client.HaalCentraalBrpClient
import nl.nlportal.haalcentraal.brp.domain.BewoningenApiRequest
import nl.nlportal.haalcentraal.brp.domain.bewoning.Bewoning
import nl.nlportal.haalcentraal.brp.domain.persoon.Persoon
import nl.nlportal.haalcentraal.brp.domain.persoon.PersoonNaam
import nl.nlportal.haalcentraal.brp.service.HaalCentraalBrpService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HaalCentraalBrpServiceImpl(
    val haalCentraalBrpClient: HaalCentraalBrpClient,
) : HaalCentraalBrpService {
    override suspend fun getPersoon(authentication: CommonGroundAuthentication): Persoon? {
        return if (authentication is BurgerAuthentication) {
            haalCentraalBrpClient.getPersoon(authentication.getBsn(), authentication)
        } else {
            null
        }
    }

    override suspend fun getBewoningen(
        authentication: CommonGroundAuthentication,
        adresseerbaarObjectIdentificatie: String,
    ): Bewoning? {
        return try {
            if (authentication is BurgerAuthentication) {
                val bewoningenApiRequest =
                    BewoningenApiRequest(
                        type = "BewoningMetPeildatum",
                        peildatum = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        adresseerbaarObjectIdentificatie = adresseerbaarObjectIdentificatie,
                    )
                haalCentraalBrpClient.getBewoningen(bewoningenApiRequest, authentication)
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }

    override suspend fun getBewonersAantal(
        authentication: CommonGroundAuthentication,
        adresseerbaarObjectIdentificatie: String,
    ): Int? {
        getBewoningen(authentication, adresseerbaarObjectIdentificatie)?.bewoningen?.first {
            return it.bewoners.size
        }

        return null
    }

    override suspend fun getGemachtigde(authentication: CommonGroundAuthentication): PersoonNaam? {
        val authenticationGemachtigde = authentication.getGemachtigde()

        return authenticationGemachtigde?.bsn?.let {
            haalCentraalBrpClient.getPersoonNaam(it, authentication)
        }
    }
}