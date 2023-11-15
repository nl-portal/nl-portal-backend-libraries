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
package com.ritense.portal.haalcentraal.hr.service

import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.haalcentraal.hr.client.HandelsregisterClient
import com.ritense.portal.haalcentraal.hr.domain.MaatschappelijkeActiviteit

class HandelsregisterService(
    private val handelsregisterClient: HandelsregisterClient,
) {

    suspend fun getMaatschappelijkeActiviteit(authentication: CommonGroundAuthentication): MaatschappelijkeActiviteit? {
        if (authentication is BedrijfAuthentication) {
            return handelsregisterClient.getMaatschappelijkeActiviteit(authentication.getKvkNummer())
        }

        return null
    }

    suspend fun getGemachtigde(authentication: CommonGroundAuthentication): MaatschappelijkeActiviteit? {
        val authenticationGemachtigde = authentication.getGemachtigde()

        return authenticationGemachtigde?.kvk?.let {
            handelsregisterClient.getMaatschappelijkeActiviteit(it)
        }
    }
}