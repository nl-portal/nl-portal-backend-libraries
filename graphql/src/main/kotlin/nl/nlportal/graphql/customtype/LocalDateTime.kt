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
package nl.nlportal.graphql.customtype

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.time.LocalDateTime

object LocalDateTimeCoercing : Coercing<LocalDateTime, String> {
    override fun parseValue(input: Any): LocalDateTime =
        runCatching {
            LocalDateTime.parse(input as? String)
        }.getOrElse {
            throw CoercingParseValueException("Expected valid LocalDateTime but was $input")
        }

    override fun parseLiteral(input: Any): LocalDateTime {
        val dateString = (input as? StringValue)?.value
        return runCatching {
            LocalDateTime.parse(dateString)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid LocalDateTime literal but was $dateString")
        }
    }

    override fun serialize(dataFetcherResult: Any): String =
        runCatching {
            dataFetcherResult.toString()
        }.getOrElse {
            throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
        }
}

internal val graphqlLocalDateTimeType =
    GraphQLScalarType.newScalar()
        .name("LocalDateTime")
        .description("A local date time")
        .coercing(LocalDateTimeCoercing)
        .build()