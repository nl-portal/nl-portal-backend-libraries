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
package com.ritense.portal.erfpachtdossier.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.erfpachtdossier.domain.Erfpachtdossier
import com.ritense.portal.erfpachtdossier.service.DossierService
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import graphql.schema.DataFetchingEnvironment

class ErfpachtdossierQuery(val dossierService: DossierService): Query {

    @GraphQLDescription("Gets all erfpachtdossiers for a user")
    suspend fun getDossiers(dfe: DataFetchingEnvironment): List<Erfpachtdossier> {
        return dossierService.getDossiers(dfe.graphQlContext.get(AUTHENTICATION_KEY))
    }

    @GraphQLDescription("Get a specific erfpachtdossier")
    suspend fun getDossier(dfe: DataFetchingEnvironment, dossierId: String): Erfpachtdossier {
        return dossierService.getDossier(dfe.graphQlContext.get(AUTHENTICATION_KEY), dossierId)
    }
}