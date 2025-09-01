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
package nl.nlportal.klant.contactmomenten.graphql

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.klant.contactmomenten.service.KlantContactMomentenService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ContactMomentQuery(
    val klantContactMomentenService: KlantContactMomentenService,
) {
    @QueryMapping
    suspend fun getKlantContactMomenten(
        authentication: CommonGroundAuthentication,
        @Argument pageNumber: Int? = 1,
    ): ContactMomentPage? =
        klantContactMomentenService.getKlantContactMomenten(
            authentication = authentication,
            page = pageNumber ?: 1,
        )

    @QueryMapping
    suspend fun getObjectContactMomenten(
        authentication: CommonGroundAuthentication,
        @Argument objectUrl: String,
        @Argument pageNumber: Int? = 1,
    ): ContactMomentPage? =
        klantContactMomentenService.getObjectContactMomenten(
            authentication = authentication,
            objectUrl = objectUrl,
            page = pageNumber ?: 1,
        )
}