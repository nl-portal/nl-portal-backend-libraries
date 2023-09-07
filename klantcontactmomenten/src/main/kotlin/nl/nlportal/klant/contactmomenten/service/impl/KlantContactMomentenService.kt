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

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.klant.contactmomenten.client.OpenKlantContactMomentenClient
import nl.nlportal.klant.contactmomenten.graphql.ContactMomentPage
import nl.nlportal.klant.contactmomenten.service.KlantContactMomentenService

class KlantContactMomentenService(
    val openKlantContactMomentenClient: OpenKlantContactMomentenClient
) : KlantContactMomentenService {

    override suspend fun getKlantContactMomenten(
        authentication: CommonGroundAuthentication,
        klant: String,
        page: Int,
        ordering: String
    ): ContactMomentPage {
        return openKlantContactMomentenClient.getContactMomenten(
            authentication,
            klant,
            page,
            ordering
        ).let { ContactMomentPage.fromResultPage(page, it) }
    }
}