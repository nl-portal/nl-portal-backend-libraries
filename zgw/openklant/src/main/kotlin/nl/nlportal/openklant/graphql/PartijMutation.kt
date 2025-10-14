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

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.graphql.domain.PartijRequest
import nl.nlportal.openklant.graphql.domain.PartijResponse
import nl.nlportal.openklant.service.OpenKlant2Service
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class PartijMutation(
    private val openklant2Service: OpenKlant2Service,
) {
    @MutationMapping
    suspend fun createUserPartij(
        authentication: CommonGroundAuthentication,
        @Argument partijRequest: PartijRequest,
    ): PartijResponse? {
        val partij =
            openklant2Service.createPartijWithIdentificator(
                authentication = authentication,
                partij = partijRequest.asOpenKlant2Partij(),
            )
        return partij?.let { PartijResponse.fromOpenKlant2Partij(partij) }
    }

    @MutationMapping
    suspend fun updateUserPartij(
        authentication: CommonGroundAuthentication,
        @Argument partijRequest: PartijRequest,
    ): PartijResponse? {
        val partij =
            openklant2Service.updatePartij(
                authentication = authentication,
                partijRequest.asOpenKlant2Partij(),
            )

        return partij?.let { PartijResponse.fromOpenKlant2Partij(partij) }
    }
}