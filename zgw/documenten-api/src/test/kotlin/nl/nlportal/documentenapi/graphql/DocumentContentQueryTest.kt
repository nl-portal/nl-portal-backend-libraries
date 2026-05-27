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

import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.UUID

@ExperimentalCoroutinesApi
internal class DocumentContentQueryTest {
    var documentenApiService: DocumentenApiService = mock()
    var environment: DataFetchingEnvironment = mock()
    var authentication: CommonGroundAuthentication = mock()
    val context: GraphQLContext = mock()
    var documentContentQuery = DocumentContentQuery(documentenApiService)

    @BeforeEach
    fun setup() {
        Mockito.`when`(environment.graphQlContext).thenReturn(context)
        Mockito.`when`(context.get<CommonGroundAuthentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
    }

    @Test
    fun getDocumentContent() =
        runTest {
            val documentId = UUID.randomUUID()
            documentContentQuery.getDocumentContent(environment, "openzaak", documentId)
            verify(documentenApiService).getDocumentContent(documentId, "openzaak")
        }
}