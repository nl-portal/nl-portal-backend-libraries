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
package nl.nlportal.haalcentraal.hr.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.haalcentraal.hr.domain.MaatschappelijkeActiviteit
import nl.nlportal.haalcentraal.hr.service.HandelsregisterService
import graphql.schema.DataFetchingEnvironment

class HandelsregisterQuery(val handelsregisterService: HandelsregisterService) : Query {
    @GraphQLDescription("Gets the bedrijf data")
    suspend fun getBedrijf(dfe: DataFetchingEnvironment): MaatschappelijkeActiviteit? {
        return handelsregisterService.getMaatschappelijkeActiviteit(dfe.graphQlContext.get(AUTHENTICATION_KEY))
    }
}