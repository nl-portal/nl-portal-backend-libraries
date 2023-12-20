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

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import nl.nlportal.zakenapi.service.ZakenApiService
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

@ExperimentalCoroutinesApi
internal class ZaakQueryTest {
    var zakenApiService: ZakenApiService = mock()
    var environment: DataFetchingEnvironment = mock()
    var authentication: CommonGroundAuthentication = mock()
    val context: GraphQLContext = mock()
    var zaakQuery = ZaakQuery(zakenApiService)

    @BeforeEach
    fun setup() {
        Mockito.`when`(environment.graphQlContext).thenReturn(context)
        Mockito.`when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
    }

    @Test
    fun getZaken() =
        runTest {
            zaakQuery.getZaken(environment, 3)
            verify(zakenApiService).getZaken(3, authentication)
        }

    @Test
    fun `getZaken no page`() =
        runTest {
            zaakQuery.getZaken(environment)
            verify(zakenApiService).getZaken(1, authentication)
        }

    @Test
    fun getZaak() =
        runTest {
            val zaakId = UUID.randomUUID()

            zaakQuery.getZaak(zaakId, environment)
            verify(zakenApiService).getZaak(zaakId, authentication)
        }
}