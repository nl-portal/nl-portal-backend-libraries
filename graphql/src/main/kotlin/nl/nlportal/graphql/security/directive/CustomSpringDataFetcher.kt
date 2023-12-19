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
package nl.nlportal.graphql.security.directive

import com.expediagroup.graphql.generator.annotations.GraphQLName
import com.expediagroup.graphql.generator.exceptions.CouldNotGetNameOfKParameterException
import com.expediagroup.graphql.generator.exceptions.InvalidWrappedTypeException
import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import com.expediagroup.graphql.generator.execution.OptionalInput
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

class CustomSpringDataFetcher(
    private val target: Any?,
    private val fn: KFunction<*>,
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
    private val applicationContext: ApplicationContext,
) : FunctionDataFetcher(target, fn) {
    override fun mapParameterToValue(
        param: KParameter,
        environment: DataFetchingEnvironment,
    ): Pair<KParameter, Any?>? =
        if (param.hasAnnotation<Autowired>()) {
            val qualifier = param.findAnnotation<Qualifier>()?.value
            if (qualifier != null) {
                param to applicationContext.getBean(qualifier)
            } else {
                param to applicationContext.getBean(param.type.javaType as Class<*>)
            }
        } else {
            when {
                param.isDataFetchingEnvironment() -> param to environment
                else -> {
                    val name = param.getName()
                    if (environment.containsArgument(name) || param.type.isOptionalInputType()) {
                        val value: Any? = environment.arguments[name]
                        param to convertArgumentToObject(param, environment, name, value)
                    } else {
                        null
                    }
                }
            }
        }

    /**
     * Convert the generic arument value from JSON input to the paramter class.
     * This is currently achieved by using a Jackson ObjectMapper.
     */
    private fun convertArgumentToObject(
        param: KParameter,
        environment: DataFetchingEnvironment,
        argumentName: String,
        argumentValue: Any?,
    ): Any? =
        when {
            param.type.isOptionalInputType() -> {
                when {
                    !environment.containsArgument(argumentName) -> OptionalInput.Undefined
                    argumentValue == null -> OptionalInput.Defined(null)
                    else -> {
                        val paramType = param.type.getTypeOfFirstArgument()
                        val value = convertValue(paramType, argumentValue)
                        OptionalInput.Defined(value)
                    }
                }
            }
            else -> convertValue(param.type, argumentValue)
        }

    private fun convertValue(
        paramType: KType,
        argumentValue: Any?,
    ): Any? =
        when {
            paramType.isList() -> {
                val argumentClass = paramType.getTypeOfFirstArgument().getJavaClass()
                val jacksonCollectionType = objectMapper.typeFactory.constructCollectionType(List::class.java, argumentClass)
                objectMapper.convertValue(argumentValue, jacksonCollectionType)
            }
            paramType.isArray() -> {
                val argumentClass = paramType.getJavaClass()
                val jacksonCollectionType = objectMapper.typeFactory.constructArrayType(argumentClass)
                objectMapper.convertValue(argumentValue, jacksonCollectionType)
            }
            else -> {
                val javaClass = paramType.getJavaClass()
                objectMapper.convertValue(argumentValue, javaClass)
            }
        }

    private fun KType.isList() = this.isSubclassOf(List::class)

    private fun KType.isArray() = this.getJavaClass().isArray

    private fun KParameter.isDataFetchingEnvironment() = this.type.classifier == DataFetchingEnvironment::class

    private fun KType.isOptionalInputType() = this.isSubclassOf(OptionalInput::class)

    private fun KType.isSubclassOf(kClass: KClass<*>) = this.jvmErasure.isSubclassOf(kClass)

    private fun KType.getJavaClass(): Class<*> = this.jvmErasure.java

    private fun KParameter.getName(): String = this.getGraphQLName() ?: this.name ?: throw CouldNotGetNameOfKParameterException(this)

    private fun KAnnotatedElement.getGraphQLName(): String? = this.findAnnotation<GraphQLName>()?.value

    @Throws(InvalidWrappedTypeException::class)
    private fun KType.getTypeOfFirstArgument(): KType = this.arguments.firstOrNull()?.type ?: throw InvalidWrappedTypeException(this)
}