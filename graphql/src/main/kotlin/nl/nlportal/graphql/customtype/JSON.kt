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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.core.util.Mapper
import graphql.language.IntValue
import graphql.language.ObjectField
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType

object JSONCoercing : Coercing<ObjectNode, ObjectNode> {

    override fun parseValue(input: Any): ObjectNode {
        if (input is ObjectValue) {
            val jsonNode: ObjectNode = JsonNodeFactory.instance.objectNode()
            for (f: ObjectField in input.objectFields) {
                jsonNode.set<JsonNode>(f.name, parse(f))
            }
            return jsonNode
        } else if (input is LinkedHashMap<*, *>) {
            return Mapper.get().convertValue(input, ObjectNode::class.java)
        }
        throw CoercingParseLiteralException("Expected valid JSON input but was $input")
    }

    override fun parseLiteral(input: Any): ObjectNode {
        val jsonString = (input as? ObjectValue)!!
        return runCatching {
            parseValue(input)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid JSON literal but was $jsonString")
        }
    }

    override fun serialize(dataFetcherResult: Any): ObjectNode = runCatching {
        Mapper.get().readValue(dataFetcherResult.toString(), ObjectNode::class.java)
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a ObjectNode")
    }

    private fun parse(o: ObjectField): JsonNode {
        if (o.value is StringValue) {
            val v = (o.value as? StringValue)?.value
            return JsonNodeFactory.instance.textNode(v)
        }
        if (o.value is IntValue) {
            val v = (o.value as? IntValue)?.value
            return JsonNodeFactory.instance.numberNode(v)
        }
        if (o.value is ObjectValue) {
            val node: ObjectNode = JsonNodeFactory.instance.objectNode()
            val v = (o.value as? ObjectValue)
            if (v != null) {
                for (f: ObjectField in v.objectFields) {
                    node.set<JsonNode>(f.name, parse(f))
                }
                return node
            }
        }
        throw IllegalStateException("unsupported type found within $o")
    }
}

internal val graphqlJSONType = GraphQLScalarType.newScalar()
    .name("JSON")
    .description("A type representing a formatted JSON")
    .coercing(JSONCoercing)
    .build()