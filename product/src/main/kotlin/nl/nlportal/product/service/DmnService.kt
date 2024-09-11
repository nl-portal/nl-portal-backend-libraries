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
package nl.nlportal.product.service

import com.jayway.jsonpath.JsonPath
import mu.KotlinLogging
import nl.nlportal.core.util.Mapper
import nl.nlportal.product.client.DmnClient
import nl.nlportal.product.domain.BeslisTabelVariable
import nl.nlportal.product.domain.DmnRequest
import nl.nlportal.product.domain.DmnRequestMapping
import nl.nlportal.product.domain.DmnResponse
import nl.nlportal.product.domain.DmnVariable
import nl.nlportal.product.domain.DmnVariableType
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductType
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class DmnService(
    val objectsApiClient: ObjectsApiClient,
    val dmnClient: DmnClient,
) {
    suspend fun getDecision(
        key: String,
        source: String,
        beslisTabelVariables: List<BeslisTabelVariable>,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): List<DmnResponse> {
        val dmnRequestMapping =
            createDmnRequest(
                key,
                source,
                beslisTabelVariables,
                dmnVariables,
            )

        return dmnClient.getDecision(dmnRequestMapping)
    }

    suspend fun getProductDecision(
        key: String,
        productId: UUID? = null,
        productTypeId: UUID,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): List<DmnResponse> {
        val productObject = getObjectsApiObjectById<Product>(productId.toString())?.record?.data

        val productType = getObjectsApiObjectById<ProductType>(productTypeId.toString())?.record?.data

        val beslisTabelVariables = productType?.beslistabellen?.get(key)
        if (beslisTabelVariables == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find a beslisTabelVariables for $key")
        }

        val sourceProduct = Mapper.get().writeValueAsString(productObject)
        val dmnRequest =
            createDmnRequest(
                key,
                sourceProduct,
                beslisTabelVariables,
                dmnVariables,
            )

        return dmnClient.getDecision(dmnRequest)
    }

    private fun createDmnRequest(
        key: String,
        sourceProduct: String? = "",
        beslisTabelVariables: List<BeslisTabelVariable>,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): DmnRequest {
        val variablesMapping = mutableMapOf<String, DmnVariable>()
        // add dmnVariables
        dmnVariables?.forEach {
            variablesMapping.put(
                it.key,
                DmnVariable(
                    it.value.value,
                    it.value.type,
                ),
            )
        }
        // map beslistabel variables with product source
        if (sourceProduct != null) {
            variablesMapping.putAll(
                mapBeslisTabelVariablesWithSource(
                    beslisTabelVariables,
                    sourceProduct,
                ),
            )
        }

        return DmnRequest(
            key = key,
            mapping =
                DmnRequestMapping(
                    variables = variablesMapping,
                ),
        )
    }

    private fun mapBeslisTabelVariablesWithSource(
        beslisTabelVariables: List<BeslisTabelVariable>,
        source: String,
    ): Map<String, DmnVariable> {
        val variablesMapping = mutableMapOf<String, DmnVariable>()
        beslisTabelVariables.forEach {
            if (it.classType != DmnVariableType.JSON) {
                if (it.value != null) {
                    variablesMapping.put(
                        it.name,
                        DmnVariable(
                            it.value,
                            it.classType,
                        ),
                    )
                } else if (it.regex != null) {
                    variablesMapping.put(
                        it.name,
                        DmnVariable(
                            findVariableInJson(it.regex, source),
                            it.classType,
                        ),
                    )
                }
            } else {
                // just put the whole source as variable
                variablesMapping.put(
                    it.name,
                    DmnVariable(
                        source,
                        it.classType,
                    ),
                )
            }
        }

        return variablesMapping
    }

    private fun findVariableInJson(
        regex: String,
        source: String,
    ): Any {
        try {
            val inputJsonPath = JsonPath.parse(source)
            return inputJsonPath.read<Any>(regex)
        } catch (ex: Exception) {
            logger.warn("Problem with parsing variable: {}", ex.message)
        }
        return ""
    }

    suspend inline fun <reified T> getObjectsApiObjectById(id: String): ObjectsApiObject<T>? {
        return objectsApiClient.getObjectById<T>(id = id)
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}