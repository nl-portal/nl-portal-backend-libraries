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
package com.ritense.portal.graphql.security.directive

import com.ritense.portal.graphql.exception.UnauthorizedException
import com.ritense.portal.graphql.security.context.SecurityConstants.AUTHENTICATION_KEY
import graphql.GraphQLContext
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.security.core.Authentication

internal class AuthenticatedDataFetcherTest {

    var environment = mock(DataFetchingEnvironment::class.java)
    var originalDataFetcher = mock(DataFetcher::class.java)

    val authenticatedDataFetcher = AuthenticatedDataFetcher(originalDataFetcher as DataFetcher<Any?>)

    @Test
    fun `should run original fetcher when authenticated`() {
        val authentication = mock(Authentication::class.java)
        val context = mock(GraphQLContext::class.java)
        val originalDataFetcherValue = "test"

        `when`(authentication.isAuthenticated).thenReturn(true)
        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)
        `when`(originalDataFetcher.get(environment)).thenReturn(originalDataFetcherValue)

        val returnedValue = authenticatedDataFetcher.get(environment)

        verify(originalDataFetcher, times(1)).get(environment)
        assertThat(returnedValue).isEqualTo(originalDataFetcherValue)
    }

    @Test
    fun `should throw exception when authentication is null`() {
        val context = mock(GraphQLContext::class.java)

        `when`(environment.graphQlContext).thenReturn(context)

        assertThrows(UnauthorizedException::class.java) {
            authenticatedDataFetcher.get(environment)
        }
    }

    @Test
    fun `should throw exception when context is null`() {
        assertThrows(UnauthorizedException::class.java) {
            authenticatedDataFetcher.get(environment)
        }
    }

    @Test
    fun `should throw exception when authentication is not authenticated`() {
        val authentication = mock(Authentication::class.java)
        val context = mock(GraphQLContext::class.java)

        `when`(authentication.isAuthenticated).thenReturn(false)
        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(AUTHENTICATION_KEY)).thenReturn(authentication)

        assertThrows(UnauthorizedException::class.java) {
            authenticatedDataFetcher.get(environment)
        }
    }
}