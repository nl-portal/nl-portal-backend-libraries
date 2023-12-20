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
package nl.nlportal.graphql.security.context

import com.expediagroup.graphql.server.spring.execution.DefaultSpringGraphQLContextFactory
import nl.nlportal.graphql.security.SecurityConstants.AUTHENTICATION_KEY
import graphql.GraphQLContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
class AuthenticationGraphQLContextFactory : DefaultSpringGraphQLContextFactory() {
    override suspend fun generateContext(request: ServerRequest): GraphQLContext {
        val reactorContext = coroutineContext[ReactorContext]?.context ?: throw RuntimeException("ReactorContext not found")

        val securityContext =
            reactorContext.getOrDefault<Mono<SecurityContext>>(
                SecurityContext::class.java,
                null,
            )!!

        val context = mutableMapOf<Any, Any>()
        securityContext.awaitFirstOrNull()?.authentication?.apply { context[AUTHENTICATION_KEY] = this }

        return super.generateContext(request).putAll(context)
    }
}