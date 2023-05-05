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
package com.ritense.portal.graphql.security

import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.server.spring.GraphQLAutoConfiguration
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.portal.graphql.security.context.AuthenticationGraphQLContextFactory
import com.ritense.portal.graphql.security.directive.ReactiveDataFactoryProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureBefore(GraphQLAutoConfiguration::class)
class GraphQLContextAutoConfiguration {

    @ExperimentalCoroutinesApi
    @Bean
    @ConditionalOnMissingBean
    fun authenticationGraphQLContextFactory(): SpringGraphQLContextFactory<*> {
        return AuthenticationGraphQLContextFactory()
    }

    @Bean
    @ConditionalOnMissingBean
    fun reactiveDataFactoryProvider(objectMapper: ObjectMapper, applicationContext: ApplicationContext): KotlinDataFetcherFactoryProvider {
        return ReactiveDataFactoryProvider(objectMapper, applicationContext)
    }
}