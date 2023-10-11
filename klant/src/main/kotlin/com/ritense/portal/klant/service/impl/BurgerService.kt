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
package com.ritense.portal.klant.service.impl

import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.klant.client.OpenKlantClient
import nl.nlportal.klant.generiek.client.OpenKlantClientConfig
import com.ritense.portal.klant.domain.klanten.Klant
import com.ritense.portal.klant.domain.klanten.KlantCreationRequest
import com.ritense.portal.klant.domain.klanten.KlantUpdate
import com.ritense.portal.klant.domain.klanten.SubjectIdentificatie
import com.ritense.portal.klant.service.BurgerService
import kotlin.random.Random

class BurgerService(
    val openKlantClientConfig: OpenKlantClientConfig,
    val openKlantClient: OpenKlantClient
) : BurgerService {

    override suspend fun getBurgerProfiel(authentication: CommonGroundAuthentication): Klant? {
        when (authentication) {
            is BurgerAuthentication -> {
                val klanten = openKlantClient.getKlanten(authentication, 1, authentication.getBsn())
                return if (klanten.isEmpty()) {
                    null
                } else if (klanten.size == 1) {
                    klanten[0]
                } else {
                    throw IllegalStateException("Multiple klanten found for BSN: ${authentication.getBsn()}")
                }
            }
            is BedrijfAuthentication -> {
                throw IllegalArgumentException("Cannot get klant by KVK")
            }
            else -> {
                throw IllegalArgumentException("Cannot get klant for this user")
            }
        }
    }

    override suspend fun updateBurgerProfiel(klantUpdate: KlantUpdate, authentication: CommonGroundAuthentication): Klant {
        if (authentication !is BurgerAuthentication) {
            throw IllegalArgumentException("Can only update burger profile for burger user")
        }

        val existingKlant = getBurgerProfiel(authentication)

        if (existingKlant == null) {
            return createBurgerProfiel(authentication, klantUpdate)
        } else {
            klantUpdate.emailadres?.let { existingKlant.emailadres = it }
            klantUpdate.telefoonnummer?.let { existingKlant.telefoonnummer = it }
            return openKlantClient.patchKlant(authentication, existingKlant.url, existingKlant)
        }
    }

    private suspend fun createBurgerProfiel(authentication: BurgerAuthentication, updatedKlant: KlantUpdate): Klant {
        val websiteUrl = "http://www.invalid-url.com/"
        val telefoonnummer = updatedKlant.telefoonnummer ?: ""
        val emailadres = updatedKlant.emailadres ?: ""

        val klantRequest = KlantCreationRequest(
            openKlantClientConfig.rsin,
            generateKlantNummer(),
            websiteUrl,
            telefoonnummer,
            emailadres,
            "natuurlijk_persoon",
            SubjectIdentificatie(
                authentication.getBsn()
            )
        )

        return openKlantClient.postKlant(authentication, klantRequest)
    }

    private fun generateKlantNummer(): String {
        // generate 8 digit random number
        return Random.nextInt(10000000, 99999999).toString()
    }
}