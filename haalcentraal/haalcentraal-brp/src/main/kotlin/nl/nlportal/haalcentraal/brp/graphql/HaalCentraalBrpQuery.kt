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
package nl.nlportal.haalcentraal.brp.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.haalcentraal.brp.domain.persoon.Persoon
import nl.nlportal.haalcentraal.brp.service.HaalCentraalBrpService
import graphql.schema.DataFetchingEnvironment

class HaalCentraalBrpQuery(val haalCentraalBrpService: HaalCentraalBrpService) : Query {
    @GraphQLDescription("Gets the persoon data")
    suspend fun getPersoon(dfe: DataFetchingEnvironment): Persoon? {
        return haalCentraalBrpService.getPersoon(dfe.graphQlContext[AUTHENTICATION_KEY])
    }

    @GraphQLDescription("Gets the number of people living in the same house of the adresseerbaarObjectIdentificatie")
    suspend fun getBewonersAantal(
        dfe: DataFetchingEnvironment,
        adresseerbaarObjectIdentificatie: String,
    ): Int? {
        return haalCentraalBrpService.getBewonersAantal(
            dfe.graphQlContext[AUTHENTICATION_KEY],
            adresseerbaarObjectIdentificatie,
        )
    }
}