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
package nl.nlportal.klant.contactmomenten.service.impl

import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.klant.client.OpenKlantClient
import nl.nlportal.klant.contactmomenten.client.KlantContactMomentenClient
import nl.nlportal.klant.contactmomenten.graphql.ContactMomentPage
import nl.nlportal.klant.contactmomenten.service.KlantContactMomentenService

class KlantContactMomentenServiceImpl(
    val klantContactMomentenClient: KlantContactMomentenClient,
    val klantClient: OpenKlantClient,
) : KlantContactMomentenService {

    override suspend fun getKlantContactMomenten(
        authentication: CommonGroundAuthentication,
        page: Int,
    ): ContactMomentPage? {
        when (authentication) {
            is BurgerAuthentication -> {
                val klanten = klantClient.getKlanten(authentication, 1, authentication.getBsn())
                return if (klanten.isEmpty()) {
                    null
                } else if (klanten.size == 1) {
                    return klantContactMomentenClient.getKlantContactMomenten(
                        authentication,
                        klanten[0].url,
                        page,
                    ).let { ContactMomentPage.fromResultPage(page, it) }
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

    override suspend fun getObjectContactMomenten(
        authentication: CommonGroundAuthentication,
        objectUrl: String,
        page: Int,
    ): ContactMomentPage {
        return klantContactMomentenClient.getObjectContactMomenten(
            authentication,
            objectUrl,
            page,
        ).let { ContactMomentPage.fromResultPage(page, it) }
    }
}