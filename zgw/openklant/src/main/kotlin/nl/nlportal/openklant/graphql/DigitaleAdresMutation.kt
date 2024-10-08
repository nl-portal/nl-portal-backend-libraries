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

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.federation.directives.AuthenticatedDirective
import com.expediagroup.graphql.server.operations.Mutation
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.openklant.graphql.domain.DigitaleAdresRequest
import nl.nlportal.openklant.graphql.domain.DigitaleAdresResponse
import nl.nlportal.openklant.service.OpenKlant2Service
import java.util.UUID

@AuthenticatedDirective
class DigitaleAdresMutation(
    private val openklant2Service: OpenKlant2Service,
) : Mutation {
    @GraphQLDescription("Create DigitaleAdres for User")
    suspend fun createUserDigitaleAdres(
        dfe: DataFetchingEnvironment,
        digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? {
        val digitaleAdres =
            openklant2Service.createDigitaleAdres(
                authentication = dfe.graphQlContext.get(AUTHENTICATION_KEY),
                digitaleAdres = digitaleAdresRequest.asOpenKlant2DigitaleAdres(),
            )
        return digitaleAdres?.let { DigitaleAdresResponse.fromOpenKlant2DigitaleAdres(digitaleAdres) }
    }

    @GraphQLDescription("Update DigitaleAdres of User")
    suspend fun updateUserDigitaleAdres(
        dfe: DataFetchingEnvironment,
        digitaleAdresId: UUID,
        digitaleAdresRequest: DigitaleAdresRequest,
    ): DigitaleAdresResponse? {
        val digitaleAdres =
            openklant2Service
                .updateDigitaleAdresById(
                    authentication = dfe.graphQlContext.get(AUTHENTICATION_KEY),
                    digitaleAdresId = digitaleAdresId,
                    digitaleAdres = digitaleAdresRequest.asOpenKlant2DigitaleAdres(),
                )

        return digitaleAdres?.let { DigitaleAdresResponse.fromOpenKlant2DigitaleAdres(digitaleAdres) }
    }

    @GraphQLDescription("Delete DigitaleAdres of User by Id")
    suspend fun deleteUserDigitaleAdres(
        dfe: DataFetchingEnvironment,
        digitaleAdresId: UUID,
    ): Boolean? {
        openklant2Service
            .deleteDigitaleAdresById(
                dfe.graphQlContext.get(AUTHENTICATION_KEY),
                digitaleAdresId,
            )
        return null
    }
}