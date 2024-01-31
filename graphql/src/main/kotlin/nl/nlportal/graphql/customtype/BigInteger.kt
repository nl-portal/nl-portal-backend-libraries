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
import java.math.BigInteger

object BigIntegerCoercing : Coercing<BigInteger, String> {
    override fun parseValue(input: Any): BigInteger =
        runCatching {
            BigInteger(input as? String)
        }.getOrElse {
            throw CoercingParseValueException("Expected valid BigInteger but was $input")
        }

    override fun parseLiteral(input: Any): BigInteger {
        val inputValue = (input as? StringValue)?.value
        return runCatching {
            BigInteger(inputValue)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid BigInteger literal but was $inputValue")
        }
    }

    override fun serialize(dataFetcherResult: Any): String =
        runCatching {
            dataFetcherResult.toString()
        }.getOrElse {
            throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
        }
}

internal val graphqlBigIntegerType =
    GraphQLScalarType.newScalar()
        .name("BigInteger")
        .description("A BigInteger")
        .coercing(BigDecimalCoercing)
        .build()