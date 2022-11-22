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
package com.ritense.portal.zaak.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.zaak.domain.documenten.DocumentContent
import com.ritense.portal.zaak.service.ZaakService
import java.util.UUID

class DocumentContentQuery(val zaakService: ZaakService) : Query {

    @GraphQLDescription("Gets a document content by id as base64 encoded")
    suspend fun getDocumentContent(id: UUID): DocumentContent {
        return zaakService.getDocumentContent(id)
    }
}