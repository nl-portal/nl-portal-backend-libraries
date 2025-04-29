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
import graphql.schema.DataFetchingEnvironment
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.haalcentraal.hr.service.HandelsregisterService
import nl.nlportal.haalcentraal2.domain.GemachtigdeV2
import nl.nlportal.haalcentraal2.service.HaalCentraal2Service

class HaalCentraal2GemachtigdeQuery(
    val haalCentraal2Service: HaalCentraal2Service,
    val handelsregisterService: HandelsregisterService,
) : Query {
    @GraphQLDescription("Gets the data of the gemachtigde")
    suspend fun getGemachtigdeV2(dfe: DataFetchingEnvironment): GemachtigdeV2 {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(AUTHENTICATION_KEY)

        return GemachtigdeV2(
            haalCentraal2Service.getGemachtigde(authentication),
            handelsregisterService.getGemachtigde(authentication),
        )
    }
}