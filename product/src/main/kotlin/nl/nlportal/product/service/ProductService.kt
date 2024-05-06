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
import mu.KLogger
import mu.KotlinLogging
import nl.nlportal.product.client.ProductConfig
import nl.nlportal.product.domain.Product
import nl.nlportal.product.domain.ProductDetails
import nl.nlportal.product.domain.ProductType
import nl.nlportal.product.domain.ProductVerbruiksObject
import nl.nlportal.product.graphql.ProductPage
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.ZakenApiConfig
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.objectenapi.domain.UpdateObjectsApiObjectRequest
import nl.nlportal.zgw.taak.autoconfigure.TaakObjectConfig
import java.util.*

class ProductService(
    val productConfig: ProductConfig,
    val objectsApiClient: ObjectsApiClient,
    val zakenApiClient: ZakenApiClient,
    val taakObjectConfig: TaakObjectConfig,
    val zakenApiConfig: ZakenApiConfig,
) {
    suspend fun getProduct(
        authentication: CommonGroundAuthentication,
        id: UUID,
    ): Product {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("rollen__initiator__identificatie", Comparator.EQUAL_TO, authentication.getUserId()),
                ObjectSearchParameter("id", Comparator.EQUAL_TO, id.toString()),
            )
        return getObjectsApiObject<Product>(
            productConfig.productTypeUrl,
            objectSearchParameters,
        ).record.data
    }

    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        productName: String,
        pageNumber: Int,
        pageSize: Int,
    ): ProductPage {
        val productType = getProductType(productName)
        val objectSearchParametersProducten =
            listOf(
                ObjectSearchParameter("rollen__initiator__identificatie", Comparator.EQUAL_TO, authentication.getUserId()),
                ObjectSearchParameter("PDCProductType", Comparator.EQUAL_TO, productType.id.toString()),
            )
        return getObjectsApiObjectResultPage<Product>(
            productConfig.productInstantieUrl,
            objectSearchParametersProducten,
            pageNumber,
            pageSize,
        ).let { ProductPage.fromResultPage(pageNumber, pageSize, it) }
    }

    suspend fun getProductVerbruiksObjecten(
        authentication: CommonGroundAuthentication,
        productId: UUID,
        pageNumber: Int,
        pageSize: Int,
    ): List<ProductVerbruiksObject> {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("rollen__initiator__identificatie", Comparator.EQUAL_TO, authentication.getUserId()),
                ObjectSearchParameter("productInstantie", Comparator.EQUAL_TO, productId.toString()),
            )
        return getObjectsApiObjectResultPage<ProductVerbruiksObject>(
            productConfig.productVerbruiksObjectTypeUrl,
            objectSearchParameters,
            pageNumber,
            pageSize,
        ).results.map { it.record.data }
    }

    suspend fun getProductZaken(
        authentication: CommonGroundAuthentication,
        productName: String,
        pageNumber: Int,
        pageSize: Int,
    ): List<Zaak> {
        // first determine the bsn or kvkNumber
        val (bsnNummer, kvkNummer) = determineAuthenticationType(authentication)
        val zaken = mutableListOf<Zaak>()

        val productType = getProductType(productName)

        // loop through the zakenTypes and get all the zaken
        productType.zaakTypen.forEach { zaakId ->
            zakenApiClient.getZaken(
                pageNumber,
                bsnNummer,
                kvkNummer,
                "${zakenApiConfig.url}/catalogi/api/v1/zaaktypen/$zaakId",
            ).results.forEach { zaak ->
                zaken.add(zaak)
            }
        }
        return zaken.take(pageSize)
    }

    suspend fun updateVerbruiksObject(
        id: UUID,
        submission: ObjectNode,
        authentication: CommonGroundAuthentication,
    ): ProductVerbruiksObject {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("rollen__initiator__identificatie", Comparator.EQUAL_TO, authentication.getUserId()),
                ObjectSearchParameter("id", Comparator.EQUAL_TO, id.toString()),
            )
        val objectsApiVerbruiksObject =
            getObjectsApiObject<ProductVerbruiksObject>(
                productConfig.productVerbruiksObjectTypeUrl,
                objectSearchParameters,
            )

        val updateRequest = UpdateObjectsApiObjectRequest.fromObjectsApiObject(objectsApiVerbruiksObject)
        updateRequest.record.data.data = submission
        updateRequest.record.correctedBy = authentication.getUserRepresentation()
        updateRequest.record.correctionFor = objectsApiVerbruiksObject.record.index.toString()

        val updatedObjectsApiTask = objectsApiClient.updateObject(objectsApiVerbruiksObject.uuid, updateRequest)
        return updatedObjectsApiTask.record.data
    }

    suspend fun getProductDetails(productInstantieId: UUID): ProductDetails? {
        return try {
            val objectSearchParameters =
                listOf(
                    ObjectSearchParameter("productInstantie", Comparator.EQUAL_TO, productInstantieId.toString()),
                )

            getObjectsApiObject<ProductDetails>(
                productConfig.productDetailsTypeUrl,
                objectSearchParameters,
            ).record.data
        } catch (ex: Exception) {
            null
        }
    }

    suspend fun getProductType(productName: String): ProductType {
        val objectSearchParameters =
            listOf(
                ObjectSearchParameter("naam", Comparator.EQUAL_TO, productName),
            )
        return getObjectsApiObject<ProductType>(
            productConfig.productTypeUrl,
            objectSearchParameters,
        ).record.data
    }

    suspend inline fun <reified T> getObjectsApiObjectById(id: String): ObjectsApiObject<T>? {
        return try {
            objectsApiClient.getObjectById<T>(id = id)
        } catch (ex: Exception) {
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
        return zakenApiClient.getZaak(zaakUUID)
    }

    private fun determineAuthenticationType(authentication: CommonGroundAuthentication): Pair<String?, String?> {
        var bsnNummer: String? = null
        var kvkNummer: String? = null
        when (authentication) {
            is BurgerAuthentication -> bsnNummer = authentication.getBsn()
            is BedrijfAuthentication -> kvkNummer = authentication.getKvkNummer()
            else -> throw IllegalArgumentException("Cannot get zaken for this user")
        }
        return bsnNummer to kvkNummer
    }

    companion object {
        val logger: KLogger = KotlinLogging.logger {}
    }
}