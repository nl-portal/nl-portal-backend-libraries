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
package nl.nlportal.haalcentraal2.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.haalcentraal2.domain.brp.BrpPersoon
import nl.nlportal.haalcentraal2.service.HaalCentraal2Service
import kotlin.text.get

class HaalCentraal2BrpQuery(val haalCentraal2Service: HaalCentraal2Service) : Query {
    @GraphQLDescription("Gets the persoon data")
    suspend fun getPersoonV2(dfe: DataFetchingEnvironment): BrpPersoon? {
        return haalCentraal2Service.getPersoon(dfe.graphQlContext[AUTHENTICATION_KEY])
    }

    @GraphQLDescription("Gets the number of people living in the same house of the adresseerbaarObjectIdentificatie")
    suspend fun getBewonersAantalV2(
        dfe: DataFetchingEnvironment,
        adresseerbaarObjectIdentificatie: String,
    ): Int? {
        return haalCentraal2Service.getBewonersAantal(
            dfe.graphQlContext[AUTHENTICATION_KEY],
            adresseerbaarObjectIdentificatie,
        )
    }
}