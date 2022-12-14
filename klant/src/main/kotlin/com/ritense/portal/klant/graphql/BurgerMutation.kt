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
package com.ritense.portal.klant.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import com.ritense.portal.klant.domain.klanten.Klant
import com.ritense.portal.klant.domain.klanten.KlantUpdate
import com.ritense.portal.klant.service.BurgerService
import com.ritense.portal.klant.validation.GraphQlValidator
import graphql.schema.DataFetchingEnvironment

class BurgerMutation(
    val burgerService: BurgerService,
    val graphQlValidator: GraphQlValidator
) : Mutation {

    @GraphQLDescription("Updates the profile for the user")
    suspend fun updateBurgerProfiel(klant: KlantUpdate, dfe: DataFetchingEnvironment): Klant? {
        graphQlValidator.validate(klant)
        return burgerService.updateBurgerProfiel(klant, dfe.graphQlContext.get(AUTHENTICATION_KEY))
    }
}