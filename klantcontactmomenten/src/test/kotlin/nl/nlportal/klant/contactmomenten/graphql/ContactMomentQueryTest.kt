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
package nl.nlportal.klant.contactmomenten.graphql

import com.nhaarman.mockitokotlin2.whenever
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.graphql.security.SecurityConstants
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.klant.contactmomenten.service.impl.KlantContactMomentenServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

@ExperimentalCoroutinesApi
internal class ContactMomentQueryTest {

    val contactMomentenService = mock(KlantContactMomentenServiceImpl::class.java)
    val contactMomentQuery = ContactMomentQuery(contactMomentenService)
    var environment = mock(DataFetchingEnvironment::class.java)
    var authentication = mock(CommonGroundAuthentication::class.java)
    val context = mock(GraphQLContext::class.java)

    @BeforeEach
    fun setup() {
        whenever(environment.graphQlContext).thenReturn(context)
        whenever(context.get<Authentication>(SecurityConstants.AUTHENTICATION_KEY)).thenReturn(authentication)
    }

    @Test
    fun getKlantContactMomenten() = runTest {
        whenever(
            contactMomentenService.getKlantContactMomenten(
                authentication,
                1,
            ),
        ).thenReturn(mock(ContactMomentPage::class.java))

        contactMomentQuery.getKlantContactMomenten(environment, 1)
        verify(contactMomentenService, times(1)).getKlantContactMomenten(authentication, 1)
    }

    @Test
    fun getObjectContactMomenten() = runTest {
        whenever(
            contactMomentenService.getObjectContactMomenten(
                authentication,
                "http://dummy.nl",
                1,
            ),
        ).thenReturn(mock(ContactMomentPage::class.java))

        contactMomentQuery.getObjectContactMomenten(environment, "http://dummy.nl", 1)
        verify(contactMomentenService, times(1)).getObjectContactMomenten(authentication, "http://dummy.nl", 1)
    }
}