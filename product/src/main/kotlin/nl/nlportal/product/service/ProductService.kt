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
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.Mapper
import nl.nlportal.product.client.ProductConfig.ProductConfigProperties
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
import nl.nlportal.zgw.taak.autoconfigure.TaakConfig.TaakConfigProperties
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.graphql.TaakPageV2
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ProductService(
    val productConfigProperties: ProductConfigProperties,
    val objectsApiTaskConfig: TaakConfigProperties,
    val objectsApiClient: ObjectsApiClient,
    val zakenApiClient: ZakenApiClient,
    val authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService,
) {
    suspend fun getProduct(
        authentication: CommonGroundAuthentication,
        id: UUID,
    ): Product? {
        val product =
            getObjectsApiObjectById<Product>(id.toString())
                ?.apply {
                    this.record.data.id = this.uuid
                }?.record
                ?.data

        if (isAuthorized(authentication, product?.rollen)) {
            return product
        }

        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied to this product")
    }

    suspend fun getProductenForProductType(
        authentication: CommonGroundAuthentication,
        productTypeId: UUID?,
    ): ProductPage {
        val objectSearchParametersProducten = getInitiatorSearchParameters(authentication).toMutableList()

        objectSearchParametersProducten.add(
            ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_TYPE, Comparator.EQUAL_TO, productTypeId.toString()),
        )

        return getObjectsApiObjectResultPage<Product>(
            productConfigProperties.productInstantieTypeUrl,
            objectSearchParametersProducten,
            1,
            2,
        ).let { ProductPage.fromResultPage(1, 2, it) }
    }

    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        productTypeId: UUID?,
        productName: String,
        productSubType: String?,
        pageNumber: Int,
        pageSize: Int,
    ): ProductPage {
        var productTypeUUID = productTypeId
        if (productTypeUUID == null) {
            productTypeUUID = getProductType(null, productName)?.id
        }
        val objectSearchParametersProducten = getInitiatorSearchParameters(authentication).toMutableList()

        objectSearchParametersProducten.add(
            ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_TYPE, Comparator.EQUAL_TO, productTypeUUID.toString()),
        )

        productSubType?.let {
            objectSearchParametersProducten.add(
                ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_SUB_PRODUCT_TYPE, Comparator.EQUAL_TO, productSubType),
            )
        }
        return getObjectsApiObjectResultPage<Product>(
            productConfigProperties.productInstantieTypeUrl,
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
                productConfigProperties.productVerbruiksObjectTypeUrl,
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
        pageSize: Int? = null,
        isOpen: Boolean? = null,
    ): List<Zaak> {
        val productType = getProductType(productTypeId, productName)

        if (productType?.zaaktypen == null || productType.zaaktypen.isEmpty()) {
            return emptyList()
        }

        val zaakTypes = productType.zaaktypen
        val request =
            zakenApiClient
                .zoeken()
                .search()
                .page(pageNumber)
                .withAuthentication(authentication)
        pageSize?.let { request.pageSize(it) }
        isOpen?.let {
            request.isOpen(isOpen)
        }

        if (!authenticationMachtigingsDienstService.isAllowedZaakTypes(authentication, zaakTypes)) {
            return emptyList()
        }

        if (zaakTypes.isNotEmpty()) {
            request.ofZaakTypes(zaakTypes.toList())
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
    ): List<TaakV2> {
        val taken =
            findTakenByIdentification(
                authentication,
                pageNumber,
                pageSize,
            )

        // when no tasks are found, just return immediately
        if (taken.isEmpty()) {
            return taken
        }

        val zaken =
            getProductZaken(
                authentication = authentication,
                productTypeId = productTypeId,
                productName = productName,
                pageNumber = pageNumber,
            )

        val producten =
            getProducten(
                authentication,
                productTypeId,
                productName,
                productSubType,
                pageNumber,
                999,
            ).content

        // filter out the taak which is not connected to a zaak or product
        return taken
            .filterNot { task ->
                !zaken.any { it.uuid.toString() == task.koppeling.value } &&
                    !producten.any { it.id.toString() == task.koppeling.value }
            }.sortedBy { it.verloopdatum }
    }

    suspend fun updateVerbruiksObject(
        id: UUID,
        submission: Any,
        authentication: CommonGroundAuthentication,
    ): ProductVerbruiksObject {
        val objectsApiVerbruiksObject =
            getObjectsApiObjectById<ProductVerbruiksObject>(id.toString()) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiVerbruiksObject)
        updateRequest.record.data.data = Mapper.get().convertValue(submission, ObjectNode::class.java)
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiVerbruiksObject.record.index.toString()

        return objectsApiClient
            .updateObject(objectsApiVerbruiksObject.uuid, updateRequest)
            .apply {
                this.record.data.id = this.uuid
            }.record.data
    }

    suspend fun getProductDetails(productInstantieId: UUID): ProductDetails? =
        try {
            val objectSearchParameters =
                listOf(
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_INSTANTIE, Comparator.EQUAL_TO, productInstantieId.toString()),
                )

            getObjectsApiObject<ProductDetails>(
                productConfigProperties.productDetailsTypeUrl,
                objectSearchParameters,
            ).apply {
                this.record.data.id = this.uuid
            }.record.data
        } catch (ex: Exception) {
            logger.error { "Something went wrong with get ProductDetails by productInstantieId $productInstantieId with error: ${ex.message}" }
            null
        }

    suspend fun getProductType(
        productTypeId: UUID?,
        productName: String,
    ): ProductType? {
        try {
            if (productTypeId != null) {
                return getObjectsApiObjectById<ProductType>(productTypeId.toString())
                    ?.apply {
                        this.record.data.id = this.uuid
                    }?.record
                    ?.data
            }
            val objectSearchParameters =
                listOf(
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_PRODUCT_NAME, Comparator.EQUAL_TO, productName),
                )
            return getObjectsApiObject<ProductType>(
                productConfigProperties.productTypeUrl,
                objectSearchParameters,
            ).apply {
                this.record.data.id = this.uuid
            }.record.data
        } catch (ex: Exception) {
            logger.error { "Something went wrong with get ProductType by name $productName or id $productTypeId with error: ${ex.message}" }
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "ProductType could not be found")
        }
    }

    suspend fun getProductTypes(authentication: CommonGroundAuthentication): List<ProductType> {
        // if user has a machtigingsdienst in their authentication return an empty list
        if (authenticationMachtigingsDienstService.hasMachtingDienst(authentication)) {
            return emptyList()
        }

        val productTypes =
            getObjectsApiObjectResultPage<ProductType>(
                productConfigProperties.productTypeUrl,
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
                getProductenForProductType(
                    authentication,
                    productType.id,
                ).content.isEmpty()
            } catch (ex: Exception) {
                true
            }
        }
    }

    suspend fun getTaken(
        authentication: CommonGroundAuthentication,
        productId: UUID,
        zaakIds: List<UUID>,
    ): List<TaakV2> {
        val taken =
            findTakenByIdentification(
                authentication,
                1,
                999,
            )

        // filter zaakIds and productId from list
        return taken
            .filterNot { task ->
                !zaakIds.any { it.toString() == task.koppeling.value } &&
                    (productId.toString() != task.koppeling.value)
            }.sortedBy { it.verloopdatum }
    }

    suspend inline fun <reified T> getObjectsApiObjectById(id: String): ObjectsApiObject<T>? =
        try {
            objectsApiClient.getObjectById<T>(id = id)
        } catch (ex: Exception) {
            logger.warn { "Something went wrong with getObjectsApiObjectById by id $id with error: ${ex.message}" }
            null
        }

    private suspend fun findTakenByIdentification(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
    ): List<TaakV2> {
        val objectSearchParameters =
            mutableListOf(
                ObjectSearchParameter("identificatie__type", Comparator.EQUAL_TO, authentication.userType),
                ObjectSearchParameter("identificatie__value", Comparator.EQUAL_TO, authentication.userId),
                ObjectSearchParameter("status", Comparator.EQUAL_TO, "open"),
            )

        authenticationMachtigingsDienstService.taakTypes(authentication)?.let {
            objectSearchParameters.add(ObjectSearchParameter("eigenaar", Comparator.IN_LIST, it.joinToString("|")))
        }

        return getObjectsApiObjectResultPage<TaakObjectV2>(
            objectsApiTaskConfig.objectTypeUrl,
            objectSearchParameters,
            pageNumber,
            pageSize,
        ).let { resultPage ->
            TaakPageV2.fromResultPage(pageNumber, pageSize, resultPage)
        }.content
    }

    private suspend inline fun <reified T> getObjectsApiObject(
        objectTypeUrl: String,
        searchParameters: List<ObjectSearchParameter>,
    ): ObjectsApiObject<T> =
        getObjectsApiObjectResultPage<T>(
            objectTypeUrl,
            searchParameters,
            1,
            2,
        ).results.single()

    private suspend inline fun <reified T> getObjectsApiObjectResultPage(
        objectTypeUrl: String,
        searchParameters: List<ObjectSearchParameter>,
        pageNumber: Int,
        pageSize: Int,
    ): ResultPage<ObjectsApiObject<T>> =
        objectsApiClient.getObjects<T>(
            objectSearchParameters = searchParameters,
            objectTypeUrl = objectTypeUrl,
            page = pageNumber,
            pageSize = pageSize,
            ordering = "-record__startAt",
        )

    suspend fun getZaak(zaakUUID: UUID): Zaak = zakenApiClient.zaken().get(zaakUUID).retrieve()

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

    suspend fun getSourceAsJson(
        key: String,
        value: UUID,
    ): String? =
        when (key) {
            "product" ->
                getObjectsApiObjectById<Product>(value.toString())
                    ?.apply {
                        this.record.data.id = this.uuid
                    }?.let {
                        Mapper.get().writeValueAsString(it.record.data)
                    }
            "productverbruiksobject" ->
                getObjectsApiObjectById<ProductVerbruiksObject>(value.toString())
                    ?.apply {
                        this.record.data.id = this.uuid
                    }?.let {
                        Mapper.get().writeValueAsString(it.record.data)
                    }
            "productdetails" ->
                getObjectsApiObjectById<ProductDetails>(value.toString())
                    ?.apply {
                        this.record.data.id = this.uuid
                    }?.let {
                        Mapper.get().writeValueAsString(it.record.data)
                    }
            else -> null
        }

    private fun getInitiatorSearchParameters(authentication: CommonGroundAuthentication): List<ObjectSearchParameter> =
        when (authentication) {
            is BurgerAuthentication -> {
                listOf(
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_IDENTIFICATIE, Comparator.EQUAL_TO, authentication.userId),
                    ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_BETROKKENETYPE, Comparator.EQUAL_TO, "natuurlijkpersoon"),
                )
            }
            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    listOf(
                        ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_IDENTIFICATIE, Comparator.EQUAL_TO, vestigingsNummer),
                        ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_BETROKKENETYPE, Comparator.EQUAL_TO, "vestiging"),
                    )
                } else {
                    listOf(
                        ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_IDENTIFICATIE, Comparator.EQUAL_TO, authentication.userId),
                        ObjectSearchParameter(OBJECT_SEARCH_PARAMETER_ROLLEN_BETROKKENETYPE, Comparator.EQUAL_TO, "nietnatuurlijkpersoon"),
                    )
                }
            }

            else -> throw IllegalArgumentException("Authentication not supported")
        }

    companion object {
        const val OBJECT_SEARCH_PARAMETER_ROLLEN_IDENTIFICATIE = "rollen__initiator__identificatie"
        const val OBJECT_SEARCH_PARAMETER_ROLLEN_BETROKKENETYPE = "rollen__initiator__betrokkeneType"
        const val OBJECT_SEARCH_PARAMETER_PRODUCT_INSTANTIE = "productInstantie"
        const val OBJECT_SEARCH_PARAMETER_PRODUCT_NAME = "naam"
        const val OBJECT_SEARCH_PARAMETER_PRODUCT_TYPE = "PDCProductType"
        const val OBJECT_SEARCH_PARAMETER_SUB_PRODUCT_TYPE = "subtype"

        val logger = KotlinLogging.logger {}
    }
}