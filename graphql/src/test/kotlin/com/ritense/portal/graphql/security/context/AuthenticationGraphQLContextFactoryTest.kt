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
package com.ritense.portal.graphql.security.context

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.util.context.Context
import kotlin.coroutines.EmptyCoroutineContext

@ExperimentalCoroutinesApi
internal class AuthenticationGraphQLContextFactoryTest {

    val authenticationGraphQLContextFactory = AuthenticationGraphQLContextFactory()
    val serverRequest = mock(ServerRequest::class.java)

    @Test
    fun `should return context with authentication when securitycontext exists in reactorcontext`() {
        val authentication = mock(Authentication::class.java)
        val securityContext = mock(SecurityContext::class.java)
        val securityContextMono = Mono.just(securityContext)

        `when`(securityContext.authentication).thenReturn(authentication)

        runBlockingTest(
            Context.of(SecurityContext::class.java, securityContextMono).asCoroutineContext()
        ) {
            val context = authenticationGraphQLContextFactory.generateContext(serverRequest)
            assertThat(context.authentication).isEqualTo(authentication)
        }
    }

    @Test
    fun `should return null authentication when securitycontext has no authentication`() {
        val securityContext = mock(SecurityContext::class.java)
        val securityContextMono = Mono.just(securityContext)

        `when`(securityContext.authentication).thenReturn(null)

        runBlockingTest(
            Context.of(SecurityContext::class.java, securityContextMono).asCoroutineContext()
        ) {
            val context = authenticationGraphQLContextFactory.generateContext(serverRequest)
            assertThat(context.authentication).isNull()
        }
    }

    @Test
    fun `should throw exception when reactorcontext does not exist`() {
        assertThrows(
            RuntimeException::class.java
        ) {
            runBlockingTest(
                EmptyCoroutineContext
            ) {
                authenticationGraphQLContextFactory.generateContext(serverRequest)
            }
        }
    }

    @Test
    fun `should throw exception when securitycontext does not exist`() {
        assertThrows(
            RuntimeException::class.java
        ) {
            runBlockingTest(
                Context.empty().asCoroutineContext()
            ) {
                authenticationGraphQLContextFactory.generateContext(serverRequest)
            }
        }
    }
}