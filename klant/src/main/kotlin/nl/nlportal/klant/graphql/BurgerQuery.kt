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
package nl.nlportal.klant.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.klant.domain.klanten.Klant
import nl.nlportal.klant.service.BurgerService
import graphql.schema.DataFetchingEnvironment

class BurgerQuery(val burgerService: BurgerService) : Query {

    @GraphQLDescription("Gets the profile for the user")
    suspend fun getBurgerProfiel(dfe: DataFetchingEnvironment): Klant? {
        return burgerService.getBurgerProfiel(dfe.graphQlContext.get(AUTHENTICATION_KEY))
    }
}