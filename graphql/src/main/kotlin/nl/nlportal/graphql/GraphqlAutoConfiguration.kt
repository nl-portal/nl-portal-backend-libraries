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
package nl.nlportal.graphql

import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring
import nl.nlportal.graphql.customtype.graphqlLocalDateTimeType
import nl.nlportal.graphql.customtype.graphqlZonedDateTimeType
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.graphql.execution.RuntimeWiringConfigurer

@AutoConfiguration
class GraphqlAutoConfiguration {
    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer =
        RuntimeWiringConfigurer { wiringBuilder: RuntimeWiring.Builder? ->
            wiringBuilder!!
                .scalar(ExtendedScalars.GraphQLBigDecimal)
                .scalar(ExtendedScalars.GraphQLBigInteger)
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.PositiveFloat)
                .scalar(ExtendedScalars.Json)
                .scalar(ExtendedScalars.LocalTime)
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.Locale)
                .scalar(ExtendedScalars.UUID)
                .scalar(graphqlZonedDateTimeType)
                .scalar(graphqlLocalDateTimeType)
        }

    @Bean
    @ConditionalOnMissingBean(GraphQLExceptionHandler::class)
    fun graphQLExceptionHandler() = GraphQLExceptionHandler()
}