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
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

class DmnService(
    val objectsApiClient: ObjectsApiClient,
    val dmnClient: DmnClient,
    val productService: ProductService,
) {
    suspend fun getDecision(
        key: String,
        source: String,
        beslisTabelVariables: List<BeslisTabelVariable>,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): List<DmnResponse> {
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
        variablesMapping.putAll(
            mapBeslisTabelVariablesWithSource(
                beslisTabelVariables,
                source,
            ),
        )
        val dmnRequest =
            createDmnRequest(
                key,
                variablesMapping,
            )
        return handleDmnResponse(
            dmnClient.getDecision(dmnRequest),
        )
    }

    suspend fun getProductDecision(
        sources: Map<String, UUID>?,
        key: String,
        productTypeId: UUID? = null,
        productName: String,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): List<DmnResponse> {
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
        val productType = productService.getProductType(productTypeId, productName)
        val beslisTabelVariableConfiguration = productType?.beslistabellen?.get(key)
        if (beslisTabelVariableConfiguration == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find a beslisTabelVariable configuration for $key")
        }

        val beslisTabelVariables = beslisTabelVariableConfiguration.variabelen

        // loop through the sources and get the Object as Json and map with the variables
        sources?.forEach {
            val source = productService.getSourceAsJson(it.key, it.value)

            if (source == null) {
                logger.warn("Could not find objects for key {} with uuid {}", it.key, it.value)
            } else {
                if (beslisTabelVariables.containsKey(it.key)) {
                    variablesMapping.putAll(
                        mapBeslisTabelVariablesWithSource(
                            beslisTabelVariables[it.key]!!,
                            source,
                        ),
                    )
                } else {
                    logger.warn("Could not find beslisTabel variables for key {}", it.key)
                }
            }
        }

        // check if the beslisTabelVariables contains a producttype configuration,
        // if yes map the variables with the json of the productType
        if (beslisTabelVariables.containsKey("producttype")) {
            variablesMapping.putAll(
                mapBeslisTabelVariablesWithSource(
                    beslisTabelVariables["producttype"]!!,
                    Mapper.get().writeValueAsString(productType),
                ),
            )
        }
        val dmnRequest =
            createDmnRequest(
                beslisTabelVariableConfiguration.key,
                variablesMapping,
            )
        return handleDmnResponse(dmnClient.getDecision(dmnRequest))
    }

    private fun handleDmnResponse(response: List<Map<String, DmnResponse>>): List<DmnResponse> {
        val resulList = mutableListOf<DmnResponse>()
        response.forEach {
            it.entries.forEach {
                it.value.name = it.key
                resulList.add(it.value)
            }
        }
        return resulList
    }

    private fun createDmnRequest(
        key: String,
        variablesMapping: Map<String, DmnVariable>,
    ): DmnRequest {
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
                if (it.regex != null) {
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