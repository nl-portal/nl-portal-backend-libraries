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
package com.ritense.portal.haalcentraal.brp.service.impl

import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.haalcentraal.brp.client.HaalCentraalBrpClient
import com.ritense.portal.haalcentraal.brp.domain.bewoning.Bewoning
import com.ritense.portal.haalcentraal.brp.domain.persoon.Persoon
import com.ritense.portal.haalcentraal.brp.domain.persoon.PersoonNaam
import com.ritense.portal.haalcentraal.brp.service.HaalCentraalBrpService

class HaalCentraalBrpService(
    val haalCentraalBrpClient: HaalCentraalBrpClient,
) : HaalCentraalBrpService {

    override suspend fun getPersoon(authentication: CommonGroundAuthentication): Persoon? {
        return if (authentication is BurgerAuthentication) {
            haalCentraalBrpClient.getPersoon(authentication.getBsn())
        } else {
            null
        }
    }

    override suspend fun getBewoningen(authentication: CommonGroundAuthentication): Bewoning? {
        return if (authentication is BurgerAuthentication) {
            haalCentraalBrpClient.getBewoningen(authentication.getBsn())
        } else {
            null
        }
    }

    override suspend fun getBewonersAantal(authentication: CommonGroundAuthentication): Int? {
        getBewoningen(authentication)?._embedded?.bewoningen?.first {
            return it.bewoners.size
        }

        return null
    }

    override suspend fun getGemachtigde(authentication: CommonGroundAuthentication): PersoonNaam? {
        val authenticationGemachtigde = authentication.getGemachtigde()

        return authenticationGemachtigde?.bsn?.let {
            haalCentraalBrpClient.getPersoonNaam(it)
        }
    }
}