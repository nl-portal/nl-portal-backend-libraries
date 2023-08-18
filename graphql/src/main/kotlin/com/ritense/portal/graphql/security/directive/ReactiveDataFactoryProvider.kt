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

import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.server.spring.execution.SpringDataFetcher
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcherFactory
import org.springframework.context.ApplicationContext
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

class ReactiveDataFactoryProvider(
    private val objectMapper: ObjectMapper,
    private val applicationContext: ApplicationContext
) :
    SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

    override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) = DataFetcherFactory {
        val isUnauthenticated = kFunction.findAnnotation<IsUnauthenticated>()
        val defaultDataFetcher = SpringDataFetcher(
            target = target,
            fn = kFunction,
            objectMapper = objectMapper,
            applicationContext = applicationContext
        )
        when {
            isUnauthenticated != null -> defaultDataFetcher
            else -> AuthenticatedDataFetcher(defaultDataFetcher)
        }
    }
}