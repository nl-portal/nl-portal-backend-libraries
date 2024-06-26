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
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.Mapper
import nl.nlportal.product.client.PrefillConfig
import nl.nlportal.product.domain.PrefillResponse
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductDetails
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequest
import nl.nlportal.zgw.objectenapi.domain.CreateObjectsApiObjectRequestRecord
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
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
    This method is called from elsewhere
     */
    suspend fun prefill(
        json: String,
        formulierUrl: String?,
    ): PrefillResponse {
        return hashAndCreatObject(json, formulierUrl)
    }

    /*
    This method is called from the ProductQuery, is part of the PDC
     */
    suspend fun prefill(
        parameters: Map<String, UUID>,
        staticData: Map<String, Any>?,
        productTypeId: UUID? = null,
        productName: String,
        formulier: String,
    ): PrefillResponse {
        // get ProductType to get the prefill data
        val productType = productService.getProductType(productTypeId, productName)

        // find prefill configuration
        val prefillConfiguration = productType?.prefill?.get(formulier)
        if (prefillConfiguration == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find a prefill configuration for $formulier")
        }

        val prefillData = mutableMapOf<String, Any>()
        staticData?.map {
            prefillData.put(it.key.replace("_", "."), it.value)
        }

        parameters.forEach {
            val jsonOfObject =
                when (it.key) {
                    PREFILL_KEY_PRODUCT ->
                        getObjectsApiObjectById<Product>(it.value.toString())?.let {
                            Mapper.get().writeValueAsString(it.record.data)
                        }
                    PREFILL_KEY_PRODUCT_DETAILS ->
                        getObjectsApiObjectById<ProductDetails>(it.value.toString())?.let {
                            Mapper.get().writeValueAsString(it.record.data)
                        }
                    PREFILL_KEY_PRODUCT_VERBUIKSOBJECT ->
                        getObjectsApiObjectById<ProductVerbruiksObject>(it.value.toString())?.let {
                            Mapper.get().writeValueAsString(it.record.data)
                        }
                    else -> null
                }

            if (jsonOfObject == null) {
                logger.warn("Could not find objects for key {} with uuid {}", it.key, it.value)
            }

            prefillData.putAll(mapPrefillVariables(prefillConfiguration.variabelen[it.key]!!, jsonOfObject!!))
        }

        val json = JsonUnflattener.unflatten(prefillData)
        return hashAndCreatObject(json, prefillConfiguration.formulierUrl)
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

    suspend inline fun <reified T> getObjectsApiObjectById(id: String): ObjectsApiObject<T>? {
        return objectsApiClient.getObjectById<T>(id = id)
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
        const val PREFILL_KEY_PRODUCT = "product"
        const val PREFILL_KEY_PRODUCT_VERBUIKSOBJECT = "productverbruiksobject"
        const val PREFILL_KEY_PRODUCT_DETAILS = "productdetails"

        val logger = KotlinLogging.logger {}
    }
}