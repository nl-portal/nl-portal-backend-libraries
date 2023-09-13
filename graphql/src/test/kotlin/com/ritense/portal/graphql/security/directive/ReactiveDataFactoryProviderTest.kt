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
package com.ritense.portal.graphql.security.directive

import com.expediagroup.graphql.server.spring.execution.SpringDataFetcher
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

internal class ReactiveDataFactoryProviderTest {

    val applicationContext = mock(ApplicationContext::class.java)
    val objectMapper = mock(ObjectMapper::class.java)
    val reactiveDataFactoryProvider = ReactiveDataFactoryProvider(objectMapper, applicationContext)

    @Test
    fun `should use authenticated data fetcher when method is not annotated`() {
        val kClass: KClass<*> = mock()
        val dataFetcher = reactiveDataFactoryProvider.functionDataFetcherFactory(null, kClass, ::authenticatedTestMethod).get(null)
        assertThat(dataFetcher).isInstanceOf(AuthenticatedDataFetcher::class.java)
    }

    @Test
    fun `should use default data fetcher when method is annotated IsUnauthenticated`() {
        val kClass: KClass<*> = mock()
        val dataFetcher = reactiveDataFactoryProvider.functionDataFetcherFactory(null, kClass, ::unauthenticatedTestMethod).get(null)
        assertThat(dataFetcher).isInstanceOf(SpringDataFetcher::class.java)
    }

    @IsUnauthenticated
    fun unauthenticatedTestMethod(): String {
        return "123"
    }

    fun authenticatedTestMethod(): String {
        return "123"
    }
}