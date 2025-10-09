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
import nl.nlportal.openklant.graphql.domain.DigitaleAdresRequest
import nl.nlportal.openklant.graphql.domain.DigitaleAdresResponse
import nl.nlportal.openklant.service.OpenKlant2Service
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class DigitaleAdresMutation(
    private val openklant2Service: OpenKlant2Service,
) {
    @MutationMapping
    suspend fun createUserDigitaleAdres(
        authentication: CommonGroundAuthentication,
        @Argument digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? {
        val digitaleAdres =
            openklant2Service.createDigitaleAdres(
                authentication = authentication,
                digitaleAdres = digitaleAdresRequest.asOpenKlant2DigitaleAdres(),
            )
        return digitaleAdres?.let { DigitaleAdresResponse.fromOpenKlant2DigitaleAdres(digitaleAdres) }
    }

    @MutationMapping
    suspend fun updateUserDigitaleAdres(
        authentication: CommonGroundAuthentication,
        @Argument digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? {
        val digitaleAdres =
            openklant2Service
                .updateDigitaleAdresById(
                    authentication = authentication,
                    digitaleAdres = digitaleAdresRequest.asOpenKlant2DigitaleAdresUpdate(),
                )

        return digitaleAdres?.let { DigitaleAdresResponse.fromOpenKlant2DigitaleAdres(digitaleAdres) }
    }

    @MutationMapping
    suspend fun deleteUserDigitaleAdres(
        authentication: CommonGroundAuthentication,
        @Argument digitaleAdresId: UUID,
    ): Boolean? {
        openklant2Service
            .deleteDigitaleAdresById(
                authentication = authentication,
                digitaleAdresId = digitaleAdresId,
            )
        return null
    }
}