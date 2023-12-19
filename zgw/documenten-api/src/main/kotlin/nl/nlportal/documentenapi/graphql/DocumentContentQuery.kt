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
package nl.nlportal.documentenapi.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.documentenapi.domain.DocumentContent
import java.util.UUID

class DocumentContentQuery(private val documentenApiService: DocumentenApiService) : Query {
    @GraphQLDescription("Gets a document content by id as base64 encoded")
    suspend fun getDocumentContent(
        documentApi: String,
        id: UUID,
    ): DocumentContent {
        return documentenApiService.getDocumentContent(id, documentApi)
    }
}