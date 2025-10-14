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
package nl.nlportal.openklant.graphql

import java.util.UUID
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.client.domain.OpenKlant2Klantcontact
import nl.nlportal.openklant.graphql.domain.OnderwerpObjectIndentificatorType
import nl.nlportal.openklant.service.OpenKlant2Service
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class KlantContactQuery(
    private val openklant2Service: OpenKlant2Service,
) {
    @QueryMapping
    suspend fun getUserKlantContacten(
        authentication: CommonGroundAuthentication,
        @Argument identificatorType: OnderwerpObjectIndentificatorType? = null,
        @Argument identificatorId: UUID? = null,
    ): List<OpenKlant2Klantcontact> =
        openklant2Service.findKlantContacten(
            authentication = authentication, // dfe.graphQlContext[AUTHENTICATION_KEY],
            identificatorType = identificatorType,
            identificatorId = identificatorId,
        )

    @QueryMapping
    suspend fun getUserKlantContact(
        @Argument klantContactId: UUID,
    ): OpenKlant2Klantcontact? =
        openklant2Service.findKlantContact(
            klantContactId = klantContactId,
        )
}