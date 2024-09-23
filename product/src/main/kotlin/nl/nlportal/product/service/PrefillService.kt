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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.wnameless.json.unflattener.JsonUnflattener
import com.jayway.jsonpath.JsonPath
import mu.KotlinLogging
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.Mapper
import nl.nlportal.product.client.PrefillConfig
import nl.nlportal.product.domain.Prefill
import nl.nlportal.product.domain.PrefillResponse
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequest
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequestRecord
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class PrefillService(
    val prefillConfig: PrefillConfig,
    val objectsApiClient: ObjectsApiClient,
    val productService: ProductService,
) {
    /*
    This method is called from elsewhere, with source json to store in ObjectsAPI
     */
    suspend fun prefill(
        json: String,
        formulierUrl: String?,
    ): PrefillResponse {
        return hashAndCreatObject(json, formulierUrl)
    }

    /*
    This method is called from elsewhere, and map variabelen and store in ObjectsAPI
     */
    suspend fun prefill(
        source: String,
        formulierUrl: String?,
        variables: Map<String, String>,
    ): PrefillResponse {
        val prefillData = mapPrefillVariables(variables, source)
        val json = JsonUnflattener.unflatten(prefillData)
        return hashAndCreatObject(json, formulierUrl)
    }

    /*
    This method is called from the ProductQuery, is part of the PDC
     */
    suspend fun prefill(
        sources: Map<String, UUID>?,
        staticData: Map<String, Any>?,
        productTypeId: UUID? = null,
        productName: String,
        formulier: String,
    ): PrefillResponse {
        val prefillData = mutableMapOf<String, Any>()
        // add staticData if available
        staticData?.map {
            prefillData.put(it.key.replace("_", "."), it.value)
        }

        // get ProductType to get the prefill data
        val productType = productService.getProductType(productTypeId, productName!!)

        // find prefill configuration
        val prefillConfiguration = productType?.prefill?.get(formulier)

        if (prefillConfiguration == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Could not find a prefill configuration for $formulier",
            )
        }

        sources?.forEach {
            val source = productService.getSourceAsJson(it.key, it.value)

            if (source == null) {
                logger.warn("Could not find objects for key {} with uuid {}", it.key, it.value)
            } else {
                if (prefillConfiguration.variabelen.containsKey(it.key)) {
                    prefillData.putAll(mapPrefillVariables(prefillConfiguration.variabelen[it.key]!!, source))
                } else {
                    logger.warn("Could not find prefill configuration variables for key {}", it.key)
                }
            }
        }

        val json = JsonUnflattener.unflatten(prefillData)
        return hashAndCreatObject(json, prefillConfiguration.formulierUrl)
    }

    fun loadJsonPrefillMapping(resourceUrl: String): Map<String, Prefill>? {
        val prefillJson = this::class.java.getResource(resourceUrl)!!.readText(Charsets.UTF_8)
        return Mapper.get().readValue(prefillJson, object : TypeReference<Map<String, Prefill>>() {})
    }

    private suspend fun hashAndCreatObject(
        json: String,
        formulierUrl: String?,
    ): PrefillResponse {
        val hash = CoreUtils.createHash(json, prefillConfig.prefillShaVersion)
        val createRequest =
            CreateObjectsApiObjectRequest(
                UUID.randomUUID(),
                prefillConfig.typeUrl,
                CreateObjectsApiObjectRequestRecord(
                    typeVersion = 1,
                    data = Mapper.get().readValue(json, ObjectNode::class.java),
                    startAt = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                ),
            )

        val prefillObject = objectsApiClient.createObject(createRequest)
        return PrefillResponse(
            objectId = prefillObject.uuid,
            hash = hash,
            formulierUrl = formulierUrl,
        )
    }

    private fun mapPrefillVariables(
        variables: Map<String, String>,
        source: String,
    ): Map<String, Any> {
        val inputJsonPath = JsonPath.parse(source)
        val mapped = mutableMapOf<String, Any>()
        variables.forEach { (k, v) ->
            try {
                mapped.put(k, inputJsonPath.read<Any>(v))
            } catch (ex: Exception) {
                logger.warn("Problem with parsing variables: {}", ex.message)
            }
        }

        return mapped
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}