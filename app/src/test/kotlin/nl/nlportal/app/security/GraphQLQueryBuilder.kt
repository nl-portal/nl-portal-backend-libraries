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
package nl.nlportal.app.security

import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLEnumType
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.GraphQLInputObjectType
import graphql.schema.GraphQLList
import graphql.schema.GraphQLNonNull
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import graphql.schema.GraphQLTypeUtil

/**
 * Builds minimal GraphQL query/mutation strings from schema field definitions.
 * Generated queries contain dummy values for required arguments and select
 * minimal return fields. The goal is to trigger the resolver's authentication
 * parameter resolution, not to produce valid business responses.
 */
object GraphQLQueryBuilder {
    fun buildQuery(field: GraphQLFieldDefinition): String {
        val args = buildArgumentString(field.arguments)
        val selection = buildSelectionSet(field.type)
        return "{ ${field.name}$args$selection }"
    }

    fun buildMutation(field: GraphQLFieldDefinition): String {
        val args = buildArgumentString(field.arguments)
        val selection = buildSelectionSet(field.type)
        return "mutation { ${field.name}$args$selection }"
    }

    private fun buildArgumentString(arguments: List<GraphQLArgument>): String {
        // Include all required arguments (NonNull without defaults)
        val requiredArgs =
            arguments.filter { arg ->
                GraphQLTypeUtil.isNonNull(arg.type) &&
                    !arg.hasSetDefaultValue()
            }
        if (requiredArgs.isEmpty()) return ""

        val argStrings =
            requiredArgs.map { arg ->
                "${arg.name}: ${generateDummyValue(arg.type)}"
            }
        return "(${argStrings.joinToString(", ")})"
    }

    private fun generateDummyValue(type: GraphQLType): String {
        val unwrapped = unwrapType(type)

        return when (unwrapped) {
            is GraphQLScalarType -> generateScalarDummy(unwrapped.name)
            is GraphQLEnumType -> unwrapped.values.firstOrNull()?.name ?: "UNKNOWN"
            is GraphQLInputObjectType -> buildInputObjectDummy(unwrapped)
            is GraphQLList -> "[${generateDummyValue(unwrapped.wrappedType)}]"
            else -> "null"
        }
    }

    private fun generateScalarDummy(scalarName: String): String =
        when (scalarName) {
            "String" -> "\"test\""
            "ID" -> "\"00000000-0000-0000-0000-000000000001\""
            "UUID" -> "\"00000000-0000-0000-0000-000000000001\""
            "Int" -> "1"
            "Float", "PositiveFloat" -> "1.0"
            "BigDecimal" -> "1.0"
            "BigInteger", "Long" -> "1"
            "Boolean" -> "true"
            "JSON" -> "\"{}\""
            "Date" -> "\"2025-01-01\""
            "DateTime", "ZonedDateTime", "LocalDateTime" -> "\"2025-01-01T00:00:00Z\""
            "LocalTime" -> "\"00:00:00\""
            "Locale" -> "\"nl\""
            else -> "\"test\""
        }

    private fun buildInputObjectDummy(type: GraphQLInputObjectType): String {
        val requiredFields =
            type.fieldDefinitions.filter { field ->
                GraphQLTypeUtil.isNonNull(field.type) &&
                    !field.hasSetDefaultValue()
            }
        // If no required fields, provide at least one field to avoid empty object
        val fieldsToInclude =
            requiredFields.ifEmpty {
                type.fieldDefinitions.take(1)
            }
        val fieldStrings =
            fieldsToInclude.map { field ->
                "${field.name}: ${generateDummyValue(field.type)}"
            }
        return "{${fieldStrings.joinToString(", ")}}"
    }

    private fun buildSelectionSet(type: GraphQLType): String {
        val unwrapped = unwrapType(type)
        return when (unwrapped) {
            is GraphQLObjectType -> " { __typename }"

            is GraphQLScalarType -> ""

            // Scalars don't need selection set
            else -> " { __typename }"
        }
    }

    private fun unwrapType(type: GraphQLType): GraphQLType =
        when (type) {
            is GraphQLNonNull -> unwrapType(type.wrappedType)
            is GraphQLList -> unwrapType(type.wrappedType)
            else -> type
        }
}