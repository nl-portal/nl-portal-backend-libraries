/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.openproduct.client.OpenProductClient
import nl.nlportal.openproduct.client.OpenProductTypeClient
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.client.domain.OpenProductProductType
import nl.nlportal.openproduct.client.domain.OpenProductProductTypeContent
import nl.nlportal.openproduct.client.domain.OpenProductProductTypesFilters
import nl.nlportal.openproduct.client.domain.OpenProductProductenFilters
import nl.nlportal.openproduct.client.domain.OpenProductThema
import nl.nlportal.openproduct.client.domain.OpenProductThemasFilters
import nl.nlportal.openproduct.client.path.ProductTypes
import nl.nlportal.openproduct.client.path.Producten
import nl.nlportal.openproduct.client.path.Themas
import nl.nlportal.openproduct.graphql.ProductTypesPage
import nl.nlportal.openproduct.graphql.ProductenPage
import nl.nlportal.openproduct.graphql.ThemasPage
import nl.nlportal.openproduct.graphql.domain.OpenProductThemaHierarchy
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.objectenapi.domain.ResultPage
import nl.nlportal.zgw.taak.autoconfigure.TaakConfig.TaakConfigProperties
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.graphql.TaakPageV2
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

class OpenProductService(
    private val openProductClient: OpenProductClient,
    private val openProductTypeClient: OpenProductTypeClient,
    private val objectsApiTaskConfigProperties: TaakConfigProperties,
    private val objectsApiClient: ObjectsApiClient,
    private val zakenApiClient: ZakenApiClient,
    private val authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService,
) {
    suspend fun getThemas(
        pageNumber: Int,
        pageSize: Int,
    ): ThemasPage {
        val searchVariables =
            listOf(
                OpenProductThemasFilters.PAGE to pageNumber.toString(),
                OpenProductThemasFilters.PAGE_SIZE to pageSize.toString(),
                OpenProductThemasFilters.GEPUBLICEERD to true,
            )
        return ThemasPage.fromResultPage(
            pageNumber = pageNumber,
            pageSize = pageSize,
            resultPage = openProductTypeClient.path<Themas>().get(searchVariables),
        )
    }

    suspend fun getHoofdThemas(): List<OpenProductThema> {
        return getThemas(1, 999).content.filter { it.hoofdThema == null }
    }

    suspend fun getThemasHierarchy(): List<OpenProductThemaHierarchy> {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()
        val themas = getThemas(1, 999).content
        val hoofdThemas = themas.toList().filter { it.hoofdThema == null }

        hoofdThemas.forEach {
            themasHierarchy.add(
                searchSubThemasHierarchy(
                    hoofdThema = it,
                    themas = themas,
                ),
            )
        }

        return themasHierarchy
    }

    suspend fun getThema(themaId: UUID): OpenProductThema? {
        try {
            return openProductTypeClient.path<Themas>().get(themaId)
        } catch (e: Exception) {
            logger.error(e) { "Error getting thema with id: $themaId" }
        }
        return null
    }

    suspend fun getProductTypes(
        pageNumber: Int,
        pageSize: Int,
        language: String,
    ): ProductTypesPage {
        val searchVariables =
            listOf(
                OpenProductProductTypesFilters.PAGE to pageNumber.toString(),
                OpenProductProductTypesFilters.PAGE_SIZE to pageSize.toString(),
                OpenProductProductTypesFilters.GEPUBLICEERD to true,
            )
        return ProductTypesPage.fromResultPage(
            pageNumber = pageNumber,
            pageSize = pageSize,
            resultPage =
                openProductTypeClient.path<ProductTypes>().get(
                    searchFilters = searchVariables,
                    language = language,
                ),
        )
    }

    suspend fun getProductType(
        productTypeId: UUID,
        language: String,
    ): OpenProductProductType? {
        try {
            return openProductTypeClient.path<ProductTypes>().get(
                productTypeId = productTypeId,
                language = language,
            )
        } catch (e: Exception) {
            logger.error(e) { "Error getting producttype with id: $productTypeId" }
        }
        return null
    }

    suspend fun getProductTypeContent(productTypeId: UUID): List<OpenProductProductTypeContent>? {
        try {
            return openProductTypeClient.path<ProductTypes>().get(
                productTypeId = productTypeId,
            )
        } catch (e: Exception) {
            logger.error(e) { "Error getting producttype content with id: $productTypeId" }
        }
        return null
    }

    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
    ): ProductenPage {
        val searchVariables =
            mutableListOf(
                OpenProductProductenFilters.PAGE to pageNumber.toString(),
                OpenProductProductenFilters.PAGE_SIZE to pageSize.toString(),
                OpenProductProductenFilters.GEPUBLICEERD to true,
            )

        searchVariables.addAll(getEigenaarFilter(authentication))

        return ProductenPage.fromResultPage(
            pageNumber = pageNumber,
            pageSize = pageSize,
            resultPage =
                openProductClient.path<Producten>().get(
                    searchFilters = searchVariables,
                ),
        )
    }

    suspend fun getProduct(
        authentication: CommonGroundAuthentication,
        productId: UUID,
    ): OpenProductProduct? {
        try {
            val product =
                openProductClient.path<Producten>().get(
                    productId = productId,
                )

            if (isAuthorizedForProduct(
                    authentication = authentication,
                    product = product!!,
                )
            ) {
                return product
            }

            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Not authorized",
            )
        } catch (e: Exception) {
            if (e is ResponseStatusException) {
                throw e
            } else {
                logger.error(e) { "Error getting product with id: $productId" }
            }
        }
        return null
    }

    suspend fun getThemaZaken(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
        isOpen: Boolean? = null,
        themaId: UUID,
        language: String,
    ): List<Zaak> {
        // 1. get themas, including the hoofdthema and may be their hoofdthema
        val themas = mutableSetOf<OpenProductThema>()

        val thema =
            getThema(
                themaId = themaId,
            )

        if (thema == null) {
            return emptyList()
        } else {
            themas.add(thema)
        }

        // 1.5 get all the hoofdthema's
        themas.addAll(searchHoofdThemasFromSubThema(thema))

        // 2. loop through productTypes and get zaakTypes
        val zaakTypes = mutableListOf<UUID>()

        themas.forEach { thema ->
            thema.producttypen.forEach {
                getProductType(
                    productTypeId = it.uuid,
                    language = language,
                )?.zaaktypen?.forEach { zaakType ->
                    zaakTypes.add(CoreUtils.extractId(zaakType.url))
                }
            }
        }

        // 3. get Zaken with filters
        val request =
            zakenApiClient.zoeken()
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

    suspend fun getThemaTaken(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
        themaId: UUID,
        language: String,
    ): List<TaakV2> {
        val taken =
            findTakenByIdentification(
                authentication = authentication,
                pageNumber = pageNumber,
                pageSize = pageSize,
            )

        // when no tasks are found, just return immediately
        if (taken.isEmpty()) {
            return taken
        }

        val zaken =
            getThemaZaken(
                authentication = authentication,
                pageNumber = pageNumber,
                pageSize = pageSize,
                themaId = themaId,
                language = language,
            )

        // filter out the taak which is not connected to a zaak
        return taken
            .filterNot { task ->
                !zaken.any { it.uuid.toString() == task.koppeling.value }
            }
            .sortedBy { it.verloopdatum }
    }

    private suspend fun findTakenByIdentification(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
    ): List<TaakV2> {
        val objectSearchParameters =
            mutableListOf(
                ObjectSearchParameter(
                    "identificatie__type",
                    nl.nlportal.zgw.objectenapi.domain.Comparator.EQUAL_TO,
                    authentication.userType,
                ),
                ObjectSearchParameter(
                    "identificatie__value",
                    nl.nlportal.zgw.objectenapi.domain.Comparator.EQUAL_TO,
                    authentication.userId,
                ),
                ObjectSearchParameter("status", nl.nlportal.zgw.objectenapi.domain.Comparator.EQUAL_TO, "open"),
            )

        authenticationMachtigingsDienstService.taakTypes(authentication)?.let {
            objectSearchParameters.add(ObjectSearchParameter("eigenaar", Comparator.IN_LIST, it.joinToString("|")))
        }

        return getObjectsApiObjectResultPage<TaakObjectV2>(
            objectsApiTaskConfigProperties.typeUrlV2,
            objectSearchParameters,
            pageNumber,
            pageSize,
        ).let { resultPage ->
            TaakPageV2.fromResultPage(pageNumber, pageSize, resultPage)
        }
            .content
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

    private suspend fun searchHoofdThemasFromSubThema(thema: OpenProductThema): List<OpenProductThema> {
        val hoofdThemas = mutableListOf<OpenProductThema>()
        if (thema.hoofdThema != null) {
            val hoofdThema =
                getThema(
                    themaId = thema.hoofdThema,
                )
            if (hoofdThema != null) {
                hoofdThemas.add(hoofdThema)
                hoofdThemas.addAll(searchHoofdThemasFromSubThema(hoofdThema))
            }
        }

        return hoofdThemas
    }

    private fun searchSubThemasHierarchy(
        hoofdThema: OpenProductThema,
        themas: List<OpenProductThema>,
    ): OpenProductThemaHierarchy {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()

        val subThemas =
            themas.toList().filter { it.hoofdThema == hoofdThema.uuid }

        subThemas.forEach {
            themasHierarchy.add(
                searchSubThemasHierarchy(
                    hoofdThema = it,
                    themas = themas,
                ),
            )
        }

        return OpenProductThemaHierarchy(
            thema = hoofdThema,
            subThemas = themasHierarchy,
        )
    }

    private fun getEigenaarFilter(authentication: CommonGroundAuthentication): List<Pair<OpenProductProductenFilters, String>> {
        return when (authentication) {
            is BurgerAuthentication -> {
                listOf(
                    Pair(
                        OpenProductProductenFilters.PAGE,
                        authentication.userId,
                    ),
                )
            }
            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    listOf(
                        Pair(
                            OpenProductProductenFilters.EIGENAREN_KVKNUMMER,
                            authentication.userId,
                        ),
                    )
                } else {
                    listOf(
                        Pair(
                            OpenProductProductenFilters.EIGENAREN_VESTIGINGSNUMMER,
                            authentication.userId,
                        ),
                        Pair(
                            OpenProductProductenFilters.EIGENAREN_KVKNUMMER,
                            authentication.userId,
                        ),
                    )
                }
            }

            else -> throw IllegalArgumentException("Authentication not supported")
        }
    }

    private fun isAuthorizedForProduct(
        authentication: CommonGroundAuthentication,
        product: OpenProductProduct,
    ): Boolean {
        return when (authentication) {
            is BurgerAuthentication -> {
                product.eigenaren.firstOrNull { it.bsn == authentication.userId } != null
            }
            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    product.eigenaren.firstOrNull { it.kvkNummer == authentication.userId } != null
                } else {
                    product.eigenaren.firstOrNull {
                        it.kvkNummer == authentication.userId &&
                            it.vestigingsnummer == vestigingsNummer
                    } != null
                }
            }

            else -> false
        }
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}