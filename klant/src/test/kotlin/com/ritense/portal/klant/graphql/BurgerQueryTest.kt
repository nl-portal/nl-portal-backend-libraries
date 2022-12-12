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

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import com.ritense.portal.klant.service.BurgerService
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

@ExperimentalCoroutinesApi
internal class BurgerQueryTest {

    var burgerService = mock(BurgerService::class.java)
    var environment = mock(DataFetchingEnvironment::class.java)
    var authentication = mock(CommonGroundAuthentication::class.java)
    val context = mock(GraphQLContext::class.java)
    var burgerQuery = BurgerQuery(burgerService)

    @BeforeEach
    fun setup() {
        Mockito.`when`(environment.graphQlContext).thenReturn(context)
        Mockito.`when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
    }

    @Test
    fun getBurgerProfiel() = runTest {
        burgerQuery.getBurgerProfiel(environment)
        verify(burgerService).getBurgerProfiel(authentication)
    }
}