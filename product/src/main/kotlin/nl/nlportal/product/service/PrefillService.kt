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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.wnameless.json.unflattener.JsonUnflattener
import com.jayway.jsonpath.JsonPath
import mu.KotlinLogging
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.Mapper
import nl.nlportal.product.client.PrefillConfig
import nl.nlportal.product.domain.PrefillConfiguration
import nl.nlportal.product.domain.PrefillObject
import nl.nlportal.product.domain.PrefillResponse
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequest
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequestRecord
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
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
        formulierUrl: String,
        key: String,
        identification: String,
    ): PrefillResponse {
        return hashAndCreatObject(json, key, formulierUrl, identification)
    }

    /*
     This method is called from elsewhere,
     and map variabelen and store in ObjectsAPI
     */
    suspend fun prefill(
        source: String,
        formulierUrl: String,
        variables: Map<String, String>,
        formulier: String,
        identification: String,
    ): PrefillResponse {
        val prefillData = mapPrefillVariables(variables, source)
        val json = JsonUnflattener.unflatten(prefillData)
        return hashAndCreatObject(json, formulier, formulierUrl, identification)
    }

    /*
     This method is called from elsewhere,
     - sources is map with sourceId and json collected from custom API
     - get prefill configuration from productType
     - map variables and store it in the Objects API
     */
    suspend fun prefill(
        sources: Map<String, String>? = null,
        key: String,
        authentication: CommonGroundAuthentication,
        productTypeId: UUID? = null,
        productName: String,
        staticData: Map<String, Any>? = null,
    ): PrefillResponse {
        val prefillData = mutableMapOf<String, Any>()
        // add staticData if available
        staticData?.map {
            prefillData.put(it.key.replace("_", "."), it.value)
        }

        // find prefill configuration
        val prefillConfiguration =
            findPrefillConfiguration(
                key,
                productTypeId,
                productName,
            )
        sources?.forEach {
            if (prefillConfiguration.variabelen.containsKey(it.key)) {
                prefillData.putAll(mapPrefillVariables(prefillConfiguration.variabelen[it.key]!!, it.value))
            } else {
                logger.warn("Could not find prefill configuration variables for source {}", it.key)
            }
        }
        if (prefillData.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, SOURCE_MAPPING_FAILED + productName)
        }
        val json = JsonUnflattener.unflatten(prefillData)
        return hashAndCreatObject(json, key, prefillConfiguration.formulierUrl, authentication.userId)
    }

    /*
    This method is called from the ProductQuery, is part of the PDC
     */
    suspend fun prefill(
        sources: Map<String, UUID>? = null,
        staticData: Map<String, Any>? = null,
        productTypeId: UUID? = null,
        productName: String,
        key: String,
        authentication: CommonGroundAuthentication,
    ): PrefillResponse {
        val prefillData = mutableMapOf<String, Any>()
        // add staticData if available
        staticData?.map {
            prefillData.put(it.key.replace("_", "."), it.value)
        }

        // find prefill configuration
        val prefillConfiguration =
            findPrefillConfiguration(
                key,
                productTypeId,
                productName,
            )
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

        if (prefillData.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, SOURCE_MAPPING_FAILED + productName)
        }

        val json = JsonUnflattener.unflatten(prefillData)
        return hashAndCreatObject(
            json,
            key,
            prefillConfiguration.formulierUrl,
            authentication.userId,
        )
    }

    private suspend fun findPrefillConfiguration(
        key: String,
        productTypeId: UUID? = null,
        productName: String,
    ): PrefillConfiguration {
        // get ProductType to get the prefill data
        val productType = productService.getProductType(productTypeId, productName)

        // find prefill configuration
        return productType?.prefillmapping?.get(key) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Could not find a prefill configuration for $key",
        )
    }

    private suspend fun hashAndCreatObject(
        json: String,
        key: String,
        formulierUrl: String,
        identification: String,
    ): PrefillResponse {
        // if in prefill config property removeObjects is TRUE, delete the objects
        if (prefillConfig.removeObjects) {
            removeObjects(
                identification,
                key,
            )
        }
        val hash = CoreUtils.createHash(json, prefillConfig.prefillShaVersion)
        val prefill =
            PrefillObject(
                identificatie = identification,
                formulier = key,
                data = Mapper.get().readValue(json, ObjectNode::class.java),
            )
        val createRequest =
            CreateObjectsApiObjectRequest(
                UUID.randomUUID(),
                prefillConfig.typeUrl,
                CreateObjectsApiObjectRequestRecord(
                    typeVersion = 1,
                    data = prefill,
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

    private suspend fun removeObjects(
        identification: String,
        formulier: String,
    ) {
        val searchParameters =
            mutableListOf(
                ObjectSearchParameter(
                    "identificatie",
                    Comparator.EQUAL_TO,
                    identification,
                ),
                ObjectSearchParameter(
                    "formulier",
                    Comparator.EQUAL_TO,
                    formulier,
                ),
            )
        val prefillObjects =
            objectsApiClient.getObjects<PrefillObject>(
                objectSearchParameters = searchParameters,
                objectTypeUrl = prefillConfig.typeUrl,
                page = 1,
                pageSize = 99,
                ordering = "-record__startAt",
            ).results

        // delete all objects found in the query
        prefillObjects.forEach {
            objectsApiClient.deleteObjectById(it.uuid)
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
        const val SOURCE_MAPPING_FAILED: String = "Source mapping failed for Prefill, check prefillmapping of productType: "
    }
}