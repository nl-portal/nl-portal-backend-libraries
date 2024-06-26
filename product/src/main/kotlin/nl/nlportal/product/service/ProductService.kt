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
import mu.KotlinLogging
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.product.client.DmnClient
import nl.nlportal.product.client.ProductConfig
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductDetails
import nl.nlportal.product.domain.ProductRol
import nl.nlportal.product.domain.ProductType
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.product.graphql.ProductPage
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import nl.nlportal.zgw.taak.domain.Taak
import nl.nlportal.zgw.taak.domain.TaakObject
import nl.nlportal.zgw.taak.graphql.TaakPage
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

class ProductService(
    val productConfig: ProductConfig,
    val objectsApiClient: ObjectsApiClient,
    val zakenApiClient: ZakenApiClient,
    val taakObjectConfig: TaakObjectConfig,
    val objectsApiTaskConfig: TaakObjectConfig,
    val dmnClient: DmnClient,
) {
    suspend fun getProduct(
        authentication: CommonGroundAuthentication,
        id: UUID,
    ): Product? {
        val product =
            getObjectsApiObjectById<Product>(id.toString())?.apply {
                this.record.data.id = this.uuid
            }?.record?.data

        if (isAuthorized(authentication, product?.rollen)) {
            return product
        }

        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this product")
    }

    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        productTypeId: UUID?,
        productName: String,
        productSubType: String?,
        pageNumber: Int,
        pageSize: Int,
    ): ProductPage {
        val productType = getProductType(productTypeId, productName)
        val objectSearchParametersProducten =
            mutableListOf(
                ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_IDENTIFICATIE, Comparator.EQUAL_TO, authentication.userId),
                ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_TYPE, Comparator.EQUAL_TO, productType?.id.toString()),
            )

        productSubType?.let {
            objectSearchParametersProducten.add(
                ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_SUB_PRODUCT_TYPE, Comparator.EQUAL_TO, productSubType),
            )
        }
        return getObjectsApiObjectResultPage<Product>(
            productConfig.productInstantieUrl,
            objectSearchParametersProducten,
            pageNumber,
            pageSize,
        ).let { ProductPage.fromResultPage(pageNumber, pageSize, it) }
    }

    suspend fun getProductVerbruiksObjecten(
        productId: String,
        pageNumber: Int,
        pageSize: Int,
    ): List<ProductVerbruiksObject> {
        return try {
            val objectSearchParameters =
                listOf(
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_INSTANTIE, Comparator.EQUAL_TO, productId),
                )
            return getObjectsApiObjectResultPage<ProductVerbruiksObject>(
                productConfig.productVerbruiksObjectTypeUrl,
                objectSearchParameters,
                pageNumber,
                pageSize,
            ).results.map {
                it.record.data.id = it.uuid
                it.record.data
            }
        } catch (ex: Exception) {
            logger.error { "Something went wrong with get Verbruiksobjecten by productInstantieId $productId with error: ${ex.message}" }
            listOf()
        }
    }

    suspend fun getProductZaken(
        authentication: CommonGroundAuthentication,
        productTypeId: UUID?,
        productName: String,
        pageNumber: Int,
        isOpen: Boolean? = null,
    ): List<Zaak> {
        val productType = getProductType(productTypeId, productName)
        val request =
            zakenApiClient.zoeken()
                .search()
                .page(pageNumber)
                .withAuthentication(authentication)
                .ofZaakTypes(productType?.zaaktypen?.map { it.toString() }!!)

        isOpen?.let {
            request.isOpen(isOpen)
        }
        return request
            .retrieve()
            .results
            .sortedBy { it.startdatum }
    }

    suspend fun getProductTaken(
        authentication: CommonGroundAuthentication,
        productTypeId: UUID?,
        productName: String,
        productSubType: String?,
        pageNumber: Int,
        pageSize: Int,
    ): List<Taak> {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("identificatie__type", Comparator.EQUAL_TO, authentication.userType),
                ObjectSearchParameter("identificatie__value", Comparator.EQUAL_TO, authentication.userId),
                ObjectSearchParameter("status", Comparator.EQUAL_TO, "open"),
            )

        val taken =
            getObjectsApiObjectResultPage<TaakObject>(
                objectsApiTaskConfig.typeUrlV2 ?: "",
                objectSearchParameters,
                pageNumber,
                pageSize,
            ).let { resultPage ->
                TaakPage.fromResultPage(pageNumber, pageSize, resultPage)
            }
                .content

        // when no tasks are found, just return immediately
        if (taken.isEmpty()) {
            return taken
        }

        val zaken =
            getProductZaken(
                authentication,
                productTypeId,
                productName,
                pageNumber,
            )

        val producten =
            getProducten(
                authentication,
                productTypeId,
                productName,
                productSubType,
                pageNumber,
                999,
            )
                .content

        // filter out the taak which is not connected to a zaak or product
        return taken
            .filterNot { task ->
                !zaken.any { it.uuid == task.zaak?.let { zaakId -> CoreUtils.extractId(zaakId) } } &&
                    !producten.any { it.taken.contains(task.id) }
            }
            .sortedBy { it.verloopdatum }
    }

    suspend fun updateVerbruiksObject(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): ProductVerbruiksObject {
        val objectsApiVerbruiksObject =
            getObjectsApiObjectById<ProductVerbruiksObject>(id.toString()) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiVerbruiksObject)
        updateRequest.record.data.data = submission
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiVerbruiksObject.record.index.toString()

        return objectsApiClient.updateObject(objectsApiVerbruiksObject.uuid, updateRequest).apply {
            this.record.data.id = this.uuid
        }.record.data
    }

    suspend fun getProductDetails(productInstantieId: UUID): ProductDetails? {
        return try {
            val objectSearchParameters =
                listOf(
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_INSTANTIE, Comparator.EQUAL_TO, productInstantieId.toString()),
                )

            getObjectsApiObject<ProductDetails>(
                productConfig.productDetailsTypeUrl,
                objectSearchParameters,
            ).apply {
                this.record.data.id = this.uuid
            }.record.data
        } catch (ex: Exception) {
            logger.error { "Something went wrong with get ProductDetails by productInstantieId $productInstantieId with error: ${ex.message}" }
            null
        }
    }

    suspend fun getProductType(
        productTypeId: UUID?,
        productName: String,
    ): ProductType? {
        try {
            if (productTypeId != null) {
                return getObjectsApiObjectById<ProductType>(productTypeId.toString())?.apply {
                    this.record.data.id = this.uuid
                }?.record?.data
            }
            val objectSearchParameters =
                listOf(
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_NAME, Comparator.EQUAL_TO, productName),
                )
            val result =
                getObjectsApiObject<ProductType>(
                    productConfig.productTypeUrl,
                    objectSearchParameters,
                ).apply {
                    this.record.data.id = this.uuid
                }.record.data
            return result
        } catch (ex: Exception) {
            logger.error { "Something went wrong with get ProductType by name $productName or id $productTypeId with error: ${ex.message}" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "ProductType could not be found")
        }
    }

    suspend fun getProductTypes(authentication: CommonGroundAuthentication): List<ProductType> {
        val productTypes =
            getObjectsApiObjectResultPage<ProductType>(
                productConfig.productTypeUrl,
                listOf(),
                1,
                999,
            ).results.map {
                it.record.data.id = it.uuid
                it.record.data
            }

        // remove if no products could be found for this productType
        return productTypes.filterNot { productType ->
            try {
                getProducten(
                    authentication,
                    productType.id,
                    productType.naam,
                    null,
                    1,
                    2,
                ).content.isEmpty()
            } catch (ex: Exception) {
                true
            }
        }
    }

    suspend inline fun <reified T> getObjectsApiObjectById(id: String): ObjectsApiObject<T>? {
        return try {
            objectsApiClient.getObjectById<T>(id = id)
        } catch (ex: Exception) {
            logger.warn { "Something went wrong with getObjectsApiObjectById by id $id with error: ${ex.message}" }
            null
        }
    }

    private suspend inline fun <reified T> getObjectsApiObject(
        objectTypeUrl: String,
        searchParameters: List<ObjectSearchParameter>,
    ): ObjectsApiObject<T> {
        return getObjectsApiObjectResultPage<T>(
            objectTypeUrl,
            searchParameters,
            1,
            2,
        ).results.single()
    }

    private suspend inline fun <reified T> getObjectsApiObjectResultPage(
        objectTypeUrl: String,
        searchParameters: List<ObjectSearchParameter>,
        pageNumber: Int,
        pageSize: Int,
    ): ResultPage<ObjectsApiObject<T>> {
        return objectsApiClient.getObjects<T>(
            objectSearchParameters = searchParameters,
            objectTypeUrl = objectTypeUrl,
            page = pageNumber,
            pageSize = pageSize,
            ordering = "-record__startAt",
        )
    }

    suspend fun getZaak(zaakUUID: UUID): Zaak {
        return zakenApiClient.zaken().get(zaakUUID).retrieve()
    }

    private fun isAuthorized(
        authentication: CommonGroundAuthentication,
        productRollen: Map<String, ProductRol>?,
    ): Boolean {
        productRollen?.forEach { (_, rol) ->
            if (rol.identificatie == authentication.userId) {
                return true
            }
        }
        return false
    }

    companion object {
        const val OBJECT_SEARCH_PARAMETER_ROLLEN_IDENTIFICATIE = "rollen__initiator__identificatie"
        const val OBJECT_SEARCH_PARAMETER_PRODUCT_INSTANTIE = "productInstantie"
        const val OBJECT_SEARCH_PARAMETER_PRODUCT_NAME = "naam"
        const val OBJECT_SEARCH_PARAMETER_PRODUCT_TYPE = "PDCProductType"
        const val OBJECT_SEARCH_PARAMETER_SUB_PRODUCT_TYPE = "subtype"

        val logger = KotlinLogging.logger {}
    }
}