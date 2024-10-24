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
import nl.nlportal.product.domain.BeslisTabelConfiguration
import nl.nlportal.product.domain.BeslisTabelVariable
import nl.nlportal.product.domain.DmnRequest
import nl.nlportal.product.domain.DmnRequestMapping
import nl.nlportal.product.domain.DmnResponse
import nl.nlportal.product.domain.DmnVariable
import nl.nlportal.product.domain.DmnVariableType
import nl.nlportal.product.domain.ProductType
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
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
    ): List<Map<String, DmnResponse>> {
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
        return dmnClient.getDecision(dmnRequest)
    }

    suspend fun getDecision(
        sources: Map<String, String>? = null,
        key: String,
        productTypeId: UUID? = null,
        productName: String,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): List<Map<String, DmnResponse>> {
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
        val beslisTabelConfiguration =
            findBeslisTabelConfiguration(
                key,
                productType,
            )

        sources?.forEach {
            if (beslisTabelConfiguration.variabelen.containsKey(it.key)) {
                variablesMapping.putAll(
                    mapBeslisTabelVariablesWithSource(
                        beslisTabelConfiguration.variabelen[it.key]!!,
                        it.value,
                    ),
                )
            } else {
                logger.warn(BESLISTABLE_NOT_FOUND_BY_KEY, it.key)
            }
        }

        if (variablesMapping.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, SOURCE_MAPPING_FAILED + productType?.naam)
        }

        val dmnRequest =
            createDmnRequest(
                beslisTabelConfiguration.key,
                variablesMapping,
            )
        return dmnClient.getDecision(dmnRequest)
    }

    suspend fun getProductDecision(
        sources: Map<String, UUID>? = null,
        key: String,
        productTypeId: UUID? = null,
        productName: String,
        dmnVariables: Map<String, DmnVariable>? = null,
    ): List<Map<String, DmnResponse>> {
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
        val beslisTabelConfiguration =
            findBeslisTabelConfiguration(
                key,
                productType,
            )

        val beslisTabelVariables = beslisTabelConfiguration.variabelen

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
                    logger.warn(BESLISTABLE_NOT_FOUND_BY_KEY, it.key)
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

        if (variablesMapping.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, SOURCE_MAPPING_FAILED + productType?.naam)
        }
        val dmnRequest =
            createDmnRequest(
                beslisTabelConfiguration.key,
                variablesMapping,
            )
        return dmnClient.getDecision(dmnRequest)
    }

    private fun findBeslisTabelConfiguration(
        key: String,
        productType: ProductType?,
    ): BeslisTabelConfiguration {
        return productType?.beslistabelmapping?.get(key) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Could not find a beslisTabelVariable configuration for $key",
        )
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

    companion object {
        val logger = KotlinLogging.logger {}
        const val SOURCE_MAPPING_FAILED: String = "Source mapping failed for DMN, check beslistabelmapping of productType: "
        const val BESLISTABLE_NOT_FOUND_BY_KEY: String = "Could not find beslisTabel variables for key {}"
    }
}