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
package nl.nlportal.zakenapi.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zakenapi.service.ZakenApiService
import graphql.schema.DataFetchingEnvironment
import java.util.UUID

class ZaakQuery(val zakenApiService: ZakenApiService) : Query {
    @GraphQLDescription(
        """
        Gets all zaken for the user
        isOpen is optional, when not available, all zaken will be returned
        isOpen is true, only zaken without enddate will be returned
        isOpen is false, only zaken with an enddate will be returned
    """,
    )
    suspend fun getZaken(
        dfe: DataFetchingEnvironment,
        page: Int? = 1,
        zaakTypeUrl: String? = null,
        isOpen: Boolean? = null,
    ): ZaakPage {
        return zakenApiService.getZaken(
            page = page!!,
            authentication = dfe.graphQlContext[AUTHENTICATION_KEY],
            zaakTypeUrl = zaakTypeUrl,
            isOpen = isOpen,
        )
    }

    @GraphQLDescription("Gets a zaak by id")
    suspend fun getZaak(
        id: UUID,
        dfe: DataFetchingEnvironment,
    ): Zaak {
        return zakenApiService.getZaak(id, dfe.graphQlContext[AUTHENTICATION_KEY])
    }
}