/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.haalcentraal.all.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.context.SecurityConstants
import com.ritense.portal.haalcentraal.brp.service.HaalCentraalBrpService
import com.ritense.portal.haalcentraal.hr.service.HandelsregisterService
import graphql.schema.DataFetchingEnvironment

class GemachtigdeQuery(
    val haalCentraalBrpService: HaalCentraalBrpService,
    val handelsregisterService: HandelsregisterService
) : Query {

    @GraphQLDescription("Gets the data of the gemachtigde")
    suspend fun getGemachtigde(dfe: DataFetchingEnvironment): Gemachtigde {
        val authentication: CommonGroundAuthentication = dfe.graphQlContext.get(SecurityConstants.AUTHENTICATION_KEY)

        return Gemachtigde(
            haalCentraalBrpService.getGemachtigde(authentication),
            handelsregisterService.getGemachtigde(authentication)
        )
    }
}