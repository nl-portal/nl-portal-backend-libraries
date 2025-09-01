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
import nl.nlportal.besluiten.service.BesluitenService
import nl.nlportal.catalogiapi.service.CatalogiApiService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

@ExperimentalCoroutinesApi
internal class ZaakQueryTest {
    var zakenApiService: ZakenApiService = mock()
    var besluitenService: BesluitenService = mock()
    var catalogiApiService: CatalogiApiService = mock()
    var authentication: CommonGroundAuthentication = mock()
    var zaakQuery = ZaakQuery(zakenApiService, besluitenService, catalogiApiService)

    @Test
    fun getZaken() =
        runTest {
            zaakQuery.getZaken(authentication, 3)
            verify(zakenApiService).getZaken(
                page = 3,
                authentication = authentication,
            )
        }

    @Test
    fun `getZaken no page`() =
        runTest {
            zaakQuery.getZaken(authentication)
            verify(zakenApiService).getZaken(
                page = 1,
                authentication = authentication,
            )
        }

    @Test
    fun getZaak() =
        runTest {
            val zaakId = UUID.randomUUID()

            zaakQuery.getZaak(zaakId, authentication)
            verify(zakenApiService).getZaak(zaakId, authentication)
        }
}
