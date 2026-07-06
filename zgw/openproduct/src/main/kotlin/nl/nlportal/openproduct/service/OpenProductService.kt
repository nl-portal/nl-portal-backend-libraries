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
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import nl.nlportal.commonground.authentication.AuthenticationMachtigingsDienstService
import nl.nlportal.commonground.authentication.BedrijfAuthentication
import nl.nlportal.commonground.authentication.BurgerAuthentication
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.core.util.CoreUtils
import nl.nlportal.core.util.CoreUtils.extractId
import nl.nlportal.documentenapi.domain.Document
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.openproduct.client.OpenProductClient
import nl.nlportal.openproduct.client.OpenProductTypeClient
import nl.nlportal.openproduct.client.domain.OpenProductActie
import nl.nlportal.openproduct.client.domain.OpenProductActiesFilters
import nl.nlportal.openproduct.client.domain.OpenProductBestand
import nl.nlportal.openproduct.client.domain.OpenProductBestandenFilters
import nl.nlportal.openproduct.client.domain.OpenProductContact
import nl.nlportal.openproduct.client.domain.OpenProductContactenFilters
import nl.nlportal.openproduct.client.domain.OpenProductLink
import nl.nlportal.openproduct.client.domain.OpenProductLinksFilters
import nl.nlportal.openproduct.client.domain.OpenProductLocatie
import nl.nlportal.openproduct.client.domain.OpenProductLocatiesFilters
import nl.nlportal.openproduct.client.domain.OpenProductOrganisatie
import nl.nlportal.openproduct.client.domain.OpenProductOrganisatiesFilters
import nl.nlportal.openproduct.client.domain.OpenProductPrijs
import nl.nlportal.openproduct.client.domain.OpenProductPrijzenFilters
import nl.nlportal.openproduct.client.domain.OpenProductProduct
import nl.nlportal.openproduct.client.domain.OpenProductProductType
import nl.nlportal.openproduct.client.domain.OpenProductProductTypeContent
import nl.nlportal.openproduct.client.domain.OpenProductProductTypesFilters
import nl.nlportal.openproduct.client.domain.OpenProductProductUpdate
import nl.nlportal.openproduct.client.domain.OpenProductProductenFilters
import nl.nlportal.openproduct.client.domain.OpenProductSchema
import nl.nlportal.openproduct.client.domain.OpenProductSchemasFilters
import nl.nlportal.openproduct.client.domain.OpenProductThema
import nl.nlportal.openproduct.client.domain.OpenProductThemasFilters
import nl.nlportal.openproduct.client.domain.OpenProductToegestaneStatus
import nl.nlportal.openproduct.client.domain.OpenProductUrl
import nl.nlportal.openproduct.client.domain.ResultPage
import nl.nlportal.openproduct.client.path.Acties
import nl.nlportal.openproduct.client.path.Bestanden
import nl.nlportal.openproduct.client.path.Contacten
import nl.nlportal.openproduct.client.path.Links
import nl.nlportal.openproduct.client.path.Locaties
import nl.nlportal.openproduct.client.path.Organisaties
import nl.nlportal.openproduct.client.path.Prijzen
import nl.nlportal.openproduct.client.path.ProductTypes
import nl.nlportal.openproduct.client.path.Producten
import nl.nlportal.openproduct.client.path.Schemas
import nl.nlportal.openproduct.client.path.Themas
import nl.nlportal.openproduct.graphql.domain.OpenProductThemaHierarchy
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.domain.Zaak
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import nl.nlportal.zgw.objectenapi.domain.Comparator
import nl.nlportal.zgw.objectenapi.domain.ObjectSearchParameter
import nl.nlportal.zgw.objectenapi.domain.ObjectsApiObject
import nl.nlportal.zgw.taak.autoconfigure.TaakConfig.TaakConfigProperties
import nl.nlportal.zgw.taak.domain.TaakObjectV2
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.graphql.TaakPageV2
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@Suppress("UNCHECKED_CAST")
class OpenProductService(
    private val openProductClient: OpenProductClient,
    private val openProductTypeClient: OpenProductTypeClient,
    private val objectsApiTaskConfigProperties: TaakConfigProperties,
    private val objectsApiClient: ObjectsApiClient,
    private val zakenApiClient: ZakenApiClient,
    private val authenticationMachtigingsDienstService: AuthenticationMachtigingsDienstService,
    private val documentenApiService: DocumentenApiService,
) {
    /**
     * Get published themas
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of themas
     */
    suspend fun getThemas(
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductThemasFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductThema> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductThemasFilters, Any>>(
                    OpenProductThemasFilters.PAGE to pageNumber.toString(),
                    OpenProductThemasFilters.PAGE_SIZE to pageSize.toString(),
                    OpenProductThemasFilters.GEPUBLICEERD to true,
                )

            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }

            return openProductTypeClient.path<Themas>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting themas with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get only published hoofd themas
     * @return: List of hoofd themas
     */
    suspend fun getHoofdThemas(): List<OpenProductThema> = getThemas(1, 999).results.filter { it.hoofdThema == null }

    /**
     * Get published hoofd themas based of the products of the authenticated user
     * @param: authentication, authenticated user
     * @return: List of themas
     */
    suspend fun getHoofdThemasByProducten(
        authentication: CommonGroundAuthentication,
    ): List<OpenProductThema> {
        try {
            val producten =
                getProducten(
                    authentication = authentication,
                    pageNumber = 1,
                    pageSize = 999,
                ).results

            if (producten.isNotEmpty()) {
                val hoofdThemas = mutableSetOf<OpenProductThema>()
                val themas =
                    getThemas(
                        pageNumber = 1,
                        pageSize = 999,
                    ).results

                // loop through producten to get via the producttypes the hoofdthemas
                producten.forEach { product ->
                    themas
                        .filter { thema ->
                            thema.producttypen.any { productType ->
                                productType.uuid == product.producttype.uuid
                            }
                        }.forEach { thema ->
                            collectThemaHierarchyUpFromSubThema(
                                themaId = thema.uuid,
                                themas = themas,
                            ).filter { collectThemaHierarchyUpFromSubThema -> collectThemaHierarchyUpFromSubThema.hoofdThema == null }
                                .let { hoofdThemaList ->
                                    hoofdThemas.addAll(hoofdThemaList)
                                }
                        }
                }

                return hoofdThemas.toList()
            }
        } catch (e: Exception) {
            logger.error { "Error getting hoofdthemas by producten with cause: " + e.message }
        }
        return emptyList()
    }

    /**
     * Get hierarchy of all published themas
     * @return: List of thema hierarchy
     */
    suspend fun getThemasHierarchy(): List<OpenProductThemaHierarchy> {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()
        val themas = getThemas(1, 999).results
        val hoofdThemas = themas.filter { it.hoofdThema == null }

        hoofdThemas.forEach {
            themasHierarchy.add(
                searchSubThemasHierarchy(
                    thema = it,
                    themas = themas,
                ),
            )
        }

        return themasHierarchy
    }

    /**
     * Get thema
     * @param: id, uuid of the thema
     * @return: thema of null
     */
    suspend fun getThema(id: UUID): OpenProductThema? {
        try {
            return openProductTypeClient.path<Themas>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting thema with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get thema hierarchy only for the requested thema
     * @param: id, uuid of thema
     * @return: List of thema hierarchy
     */
    suspend fun getThemaHierarchy(id: UUID): List<OpenProductThemaHierarchy> {
        try {
            val thema =
                openProductTypeClient.path<Themas>().get(
                    id = id,
                )
            if (thema != null) {
                return buildThemaHierachy(thema = thema)
            }
        } catch (e: Exception) {
            logger.error { "Error building thema hierarchy id: $id with cause: " + e.message }
        }
        return emptyList()
    }

    /**
     * Get productTypes
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: language, language of the producttype, default is NL
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of productTypes
     */
    suspend fun getProductTypes(
        pageNumber: Int,
        pageSize: Int,
        language: String? = null,
        extraSearchVariables: List<Pair<OpenProductProductTypesFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductProductType> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductProductTypesFilters, Any>>(
                    OpenProductProductTypesFilters.PAGE to pageNumber.toString(),
                    OpenProductProductTypesFilters.PAGE_SIZE to pageSize.toString(),
                    OpenProductProductTypesFilters.GEPUBLICEERD to true,
                )

            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<ProductTypes>().get(
                searchFilters = searchVariables,
                language = language,
            )
        } catch (e: Exception) {
            logger.error { "Error getting productTypes with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get producttype
     * @param: id, uuid of the producttype
     * @param: language, language of productype, is optional
     * @return: Producttype or null
     */
    suspend fun getProductType(
        id: UUID,
        language: String? = null,
    ): OpenProductProductType? {
        try {
            return openProductTypeClient.path<ProductTypes>().get(
                id = id,
                language = language,
            )
        } catch (e: Exception) {
            logger.error { "Error getting producttype with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get acties
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of acties
     */
    suspend fun getActies(
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductActiesFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductActie> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductActiesFilters, Any>>(
                    OpenProductActiesFilters.PAGE to pageNumber.toString(),
                    OpenProductActiesFilters.PAGE_SIZE to pageSize.toString(),
                )

            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Acties>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting acties with cause: $e.message" }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get actie
     * @param: id, uuid of actie
     * @return: actie or null
     */
    suspend fun getActie(id: UUID): OpenProductActie? {
        try {
            return openProductTypeClient.path<Acties>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting actie with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get bestanden
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: naam, naam of bestand, is optional
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of bestanden
     */
    suspend fun getBestanden(
        pageNumber: Int,
        pageSize: Int,
        naam: String? = null,
        extraSearchVariables: List<Pair<OpenProductBestandenFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductBestand> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductBestandenFilters, Any>>(
                    OpenProductBestandenFilters.PAGE to pageNumber.toString(),
                    OpenProductBestandenFilters.PAGE_SIZE to pageSize.toString(),
                )

            naam?.let {
                searchVariables.add(OpenProductBestandenFilters.NAAM_CONTAINS to it)
            }

            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Bestanden>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting bestanden with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get bestand
     * @param: id, uuid of bestand
     * @return: bestand or null
     */
    suspend fun getBestand(id: UUID): OpenProductBestand? {
        try {
            return openProductTypeClient.path<Bestanden>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting bestand with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get conatcten
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: naam, naam of contact, is optional
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of contacten
     */
    suspend fun getContacten(
        pageNumber: Int,
        pageSize: Int,
        naam: String? = null,
        extraSearchVariables: List<Pair<OpenProductContactenFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductContact> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductContactenFilters, Any>>(
                    OpenProductContactenFilters.PAGE to pageNumber.toString(),
                    OpenProductContactenFilters.PAGE_SIZE to pageSize.toString(),
                )

            naam?.let {
                searchVariables.add(OpenProductContactenFilters.NAAM to it)
            }

            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Contacten>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting contacten with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get contact
     * @param: id, uuid of contact
     * @return: contact or null
     */
    suspend fun getContact(id: UUID): OpenProductContact? {
        try {
            return openProductTypeClient.path<Contacten>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting contact with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get locaties
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: naam, naam of locatie, is optional
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of locaties
     */
    suspend fun getLocaties(
        pageNumber: Int,
        pageSize: Int,
        naam: String? = null,
        extraSearchVariables: List<Pair<OpenProductLocatiesFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductLocatie> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductLocatiesFilters, Any>>(
                    OpenProductLocatiesFilters.PAGE to pageNumber.toString(),
                    OpenProductLocatiesFilters.PAGE_SIZE to pageSize.toString(),
                )

            naam?.let {
                searchVariables.add(OpenProductLocatiesFilters.NAAM_EXACT to it)
            }
            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Locaties>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting locaties with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get locatie
     * @param: id, uuid of locatie
     * @return: locatie or null
     */
    suspend fun getLocatie(id: UUID): OpenProductLocatie? {
        try {
            return openProductTypeClient.path<Locaties>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting locatie with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get organisaties
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: naam, naam of organisatie, is optional
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of organisaties
     */
    suspend fun getOrganisaties(
        pageNumber: Int,
        pageSize: Int,
        naam: String? = null,
        extraSearchVariables: List<Pair<OpenProductOrganisatiesFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductOrganisatie> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductOrganisatiesFilters, Any>>(
                    OpenProductOrganisatiesFilters.PAGE to pageNumber.toString(),
                    OpenProductOrganisatiesFilters.PAGE_SIZE to pageSize.toString(),
                )

            naam?.let {
                searchVariables.add(OpenProductOrganisatiesFilters.NAAM_EXACT to it)
            }
            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Organisaties>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting organisaties with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get organisatie
     * @param: id, uuid of organisatie
     * @return: organisatie or null
     */
    suspend fun getOrganisatie(id: UUID): OpenProductOrganisatie? {
        try {
            return openProductTypeClient.path<Organisaties>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting organisatie with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get prijzen
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of prijzen
     */
    suspend fun getPrijzen(
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductPrijzenFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductPrijs> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductPrijzenFilters, Any>>(
                    OpenProductPrijzenFilters.PAGE to pageNumber.toString(),
                    OpenProductPrijzenFilters.PAGE_SIZE to pageSize.toString(),
                )
            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Prijzen>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting prijzen with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get prijs
     * @param: id, uuid if prijs
     * @return: prijs or null
     */
    suspend fun getPrijs(id: UUID): OpenProductPrijs? {
        try {
            return openProductTypeClient.path<Prijzen>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting prijs with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get schemas
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of schemas
     */
    suspend fun getSchemas(
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductSchemasFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductSchema> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductSchemasFilters, Any>>(
                    OpenProductSchemasFilters.PAGE to pageNumber.toString(),
                    OpenProductSchemasFilters.PAGE_SIZE to pageSize.toString(),
                )
            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Schemas>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting schemas with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get schema
     * @param: id, uuid of schema
     * @return: schema or null
     */
    suspend fun getSchema(id: UUID): OpenProductSchema? {
        try {
            return openProductTypeClient.path<Schemas>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting schema with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get links
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: extraSearchVariables, extra search variables to extend the search
     * @return: Result of links
     */
    suspend fun getLinks(
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductLinksFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductLink> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductLinksFilters, Any>>(
                    OpenProductLinksFilters.PAGE to pageNumber.toString(),
                    OpenProductLinksFilters.PAGE_SIZE to pageSize.toString(),
                )
            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Links>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting links with cause: " + e.message }
        }

        return ResultPage(
            count = 0,
            results = emptyList(),
        )
    }

    /**
     * Get link
     * @param: id, uuid of link
     * @return: link or null
     */
    suspend fun getLink(id: UUID): OpenProductLink? {
        try {
            return openProductTypeClient.path<Links>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting link with id: $id with cause: " + e.message }
        }
        return null
    }

    /**
     * Get content of producttype
     * @param: productTypeId, producttype id
     * @return: List of producttype content
     */
    suspend fun getProductTypeContent(productTypeId: UUID): List<OpenProductProductTypeContent>? {
        try {
            return openProductTypeClient.path<ProductTypes>().get(
                id = productTypeId,
            )
        } catch (e: Exception) {
            logger.error { "Error getting producttype content with id: $productTypeId with cause: " + e.message }
        }
        return null
    }

    /**
     * Get producten
     * @param: pageNumber, number for pagination
     * @param: pageSize, size for pagination
     * @param: extraSearchVariables, extra search variables to extend the search
     * @param: status, status of product
     * @param: productTypeCode, producttype code
     * @param: productTypeId, producttype uuid
     * @return: Result of producten
     */
    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductProductenFilters, String>> = emptyList(),
        status: OpenProductToegestaneStatus? = null,
        productTypeCode: String? = null,
        productTypeId: String? = null,
        productTypeCodes: List<String>? = null,
        productTypeIds: List<String>? = null,
    ): ResultPage<OpenProductProduct> {
        val searchVariables =
            mutableListOf(
                OpenProductProductenFilters.PAGE to pageNumber.toString(),
                OpenProductProductenFilters.PAGE_SIZE to pageSize.toString(),
                OpenProductProductenFilters.GEPUBLICEERD to true,
            )

        searchVariables.addAll(getEigenaarFilter(authentication))

        if (extraSearchVariables.isNotEmpty()) {
            searchVariables.addAll(extraSearchVariables)
        }

        status?.let {
            searchVariables.add(OpenProductProductenFilters.STATUS to it.toString().lowercase())
        }

        productTypeCode?.let {
            searchVariables.add(OpenProductProductenFilters.PRODUCTTYPE_CODE to it)
        }

        productTypeCodes?.let {
            searchVariables.add(OpenProductProductenFilters.PRODUCTTYPE_CODE_IN to it.joinToString(","))
        }

        productTypeId?.let {
            searchVariables.add(OpenProductProductenFilters.PRODUCTTYPE_UUID to it)
        }

        productTypeIds?.let {
            searchVariables.add(OpenProductProductenFilters.PRODUCTTYPE_UUID_IN to it.joinToString(","))
        }

        return openProductClient.path<Producten>().get(
            searchFilters = searchVariables,
        )
    }

    /**
     * Get product
     * @param: id, uuid of product
     * @return: product or null
     */
    suspend fun getProduct(
        authentication: CommonGroundAuthentication,
        id: UUID,
    ): OpenProductProduct? {
        try {
            val product =
                openProductClient.path<Producten>().get(
                    id = id,
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
                logger.error { "Error getting product with id: $id with cause: " + e.message }
            }
        }
        return null
    }

    /**
     * Get producten by thema
     * @param: authentication, authenticated user
     * @param: themaId, uuid of the thema
     * @return: producten list of thema
     */
    suspend fun getProductenByThema(
        authentication: CommonGroundAuthentication,
        themaId: UUID,
    ): List<OpenProductProduct> {
        try {
            val thema = getThema(id = themaId)
            if (thema == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find thema with id: $themaId")
            }

            return getThemaProducten(
                authentication = authentication,
                thema = thema,
            )
        } catch (e: Exception) {
            logger.error { "Error getting products by thema id: $themaId with cause: " + e.message }
        }

        return emptyList()
    }

    /**
     * Get producten by thema
     * @param: authentication, authenticated user
     * @param: thema
     * @return: producten list of thema
     */
    suspend fun getThemaProducten(
        authentication: CommonGroundAuthentication,
        thema: OpenProductThema,
    ): List<OpenProductProduct> {
        try {
            val searchVariables =
                listOf(
                    OpenProductProductenFilters.THEMA_UUID to thema.uuid.toString(),
                )
            return getProducten(
                authentication = authentication,
                pageNumber = 1,
                pageSize = 999,
                extraSearchVariables = searchVariables,
            ).results
        } catch (e: Exception) {
            logger.error { "Error getting products by thema id: ${thema.uuid} with cause: " + e.message }
        }

        return emptyList()
    }

    suspend fun getThemaLinks(
        thema: OpenProductThema,
    ): List<OpenProductLink> {
        val links = mutableListOf<OpenProductLink>()

        thema.producttypen.forEach { productType ->
            val searchVariables =
                listOf<Pair<OpenProductLinksFilters, Any>>(
                    OpenProductLinksFilters.PAGE to 1,
                    OpenProductLinksFilters.PAGE_SIZE to 999,
                    OpenProductLinksFilters.PRODUCTTYPE_UUID to productType.uuid.toString(),
                )

            val linkResponse = openProductTypeClient.path<Links>().get(searchVariables)
            if (linkResponse.results.isNotEmpty()) {
                links.addAll(linkResponse.results)
            }
        }

        return links
    }

    /**
     * Update product verbruiksobject or dataobject
     * @param: authentication, authenticated user
     * @param: productUpdate, product to update
     * @return: updated product
     */
    suspend fun updateProduct(
        authentication: CommonGroundAuthentication,
        productUpdate: OpenProductProductUpdate,
    ): OpenProductProduct? {
        // get product, only to check if user authorized to perform this update
        getProduct(
            authentication = authentication,
            id = productUpdate.uuid,
        )

        return openProductClient.path<Producten>().patch(productUpdate = productUpdate)
    }

    /**
     * Get zaken of a thema
     * @param: authentication, authenticated user
     * @param: isOpen, boolean if a zaak is still open
     * @param: id, uuid of thema,
     * @param: language, language of the producttype, default is NL
     * @param: themaList, list of parent themas
     * @param: themas: all published themas
     * @return: list of zaken
     */
    suspend fun getThemaZaken(
        authentication: CommonGroundAuthentication,
        isOpen: Boolean? = null,
        id: UUID,
        language: String? = null,
        themasList: List<OpenProductThema>? = null,
        themas: List<OpenProductThema> = emptyList(),
        pageSize: Int? = null,
    ): List<Zaak> {
        val themasAll = mutableListOf<OpenProductThema>()
        val collectedThemaList = mutableListOf<OpenProductThema>()
        // only do get the collectThemaHierarchyUpFromSubThema if themasList == null otherwise it is unnecessary
        if (themasList == null) {
            if (themas.isEmpty()) {
                themasAll.addAll(
                    getThemas(
                        pageNumber = 1,
                        pageSize = 999,
                    ).results,
                )
            } else {
                themasAll.addAll(themas)
            }

            collectedThemaList.addAll(
                collectThemaHierarchyUpFromSubThema(
                    themaId = id,
                    themas = themasAll,
                ),
            )
        } else {
            collectedThemaList.addAll(themasList)
        }
        // 2. loop through productTypes and get zaakTypes
        val zaakTypes = mutableSetOf<UUID>()

        collectedThemaList.forEach { thema ->
            thema.producttypen.forEach {
                getProductType(
                    id = it.uuid,
                    language = language,
                )?.zaaktypen?.forEach { zaakType ->
                    zaakTypes.add(CoreUtils.extractId(zaakType.url!!))
                }
            }
        }

        // 3. get Zaken with filters
        val request =
            zakenApiClient
                .zoeken()
                .search()
                .page(1)
                .pageSize(pageSize ?: 999)
                .withAuthentication(authentication)
        isOpen?.let {
            request.isOpen(isOpen)
        }

        if (!authenticationMachtigingsDienstService.isAllowedZaakTypes(authentication, zaakTypes.toList())) {
            return emptyList()
        }

        if (zaakTypes.isNotEmpty()) {
            request.ofZaakTypes(zaakTypes.toList())
        }

        return request
            .retrieve()
            .results
    }

    /**
     * Get taken of a thema
     * @param: authentication, authenticated user
     * @param: id, uuid of thema,
     * @param: language, language of the producttype, default is NL
     * @return: list of taken
     */
    suspend fun getThemaTaken(
        authentication: CommonGroundAuthentication,
        id: UUID,
        language: String? = null,
        pageSize: Int? = null,
    ): List<TaakV2> {
        val themas =
            getThemas(
                pageNumber = 1,
                pageSize = 999,
            ).results
        val collectedThemas =
            collectThemaHierarchyUpFromSubThema(
                themaId = id,
                themas = themas,
            )
        val taken =
            findTakenByIdentification(
                authentication = authentication,
                pageNumber = 1,
                pageSize = pageSize ?: 999,
            )

        // when no tasks are found, just return immediately
        if (taken.isEmpty()) {
            return taken
        }

        val zaken =
            getThemaZaken(
                authentication = authentication,
                id = id,
                language = language,
                themasList = collectedThemas,
                themas = themas,
            )

        val searchVariables =
            listOf(
                OpenProductProductenFilters.THEMA_UUID_IN to themas.joinToString { it.uuid.toString() },
            )

        val producten =
            getProducten(
                authentication = authentication,
                pageNumber = 1,
                pageSize = 999,
                extraSearchVariables = searchVariables,
            ).results

        // filter out the taak which is not connected to a zaak
        return taken
            .filterNot { task ->
                !zaken.any { it.uuid.toString() == task.koppeling.value } &&
                    !producten.any { it.uuid.toString() == task.koppeling.value }
            }.sortedBy { it.verloopdatum }
    }

    /**
     * Get zaken of a product
     * @param: zaken, list of zaken
     * @return: list of zaken
     */
    suspend fun getProductZaken(zaken: List<OpenProductUrl>): List<Zaak> {
        val zaakList = mutableListOf<Zaak>()
        zaken.forEach {
            try {
                zaakList.add(zakenApiClient.zaken().get(CoreUtils.extractId(it.url!!)).retrieve())
            } catch (e: Exception) {
                logger.error(e) { "Error while fetching product zaken: " + e.message }
            }
        }
        return zaakList
    }

    /**
     * Get taken of a product
     * @param: taken, list of taken
     * @return: list of taken
     */
    suspend fun getProductTaken(
        taken: List<OpenProductUrl>,
    ): List<TaakV2> {
        val takenList = mutableListOf<TaakV2>()
        taken.forEach {
            try {
                val taakObject = objectsApiClient.getObjectById<TaakObjectV2>(CoreUtils.extractId(it.url!!).toString())
                if (taakObject != null) {
                    takenList.add(TaakV2.fromObjectsApi(taakObject))
                }
            } catch (e: Exception) {
                logger.error { "Error getting product taken: " + e.message }
            }
        }

        return takenList
    }

    /**
     * Get acties of a product
     * @param: productTypeId, uuid of producttype
     * @param: naam, naam of the acties
     * @return: list of acties
     */
    suspend fun getProductActies(
        productTypeId: UUID,
        naam: String? = null,
    ): List<OpenProductActie> {
        try {
            val extraSearchVariables =
                mutableListOf<Pair<OpenProductActiesFilters, Any>>(
                    OpenProductActiesFilters.PRODUCTTYPE_UUID to productTypeId.toString(),
                )

            naam?.let {
                extraSearchVariables.add(
                    OpenProductActiesFilters.NAAM to naam,
                )
            }
            return getActies(
                pageNumber = 1,
                pageSize = 999,
                extraSearchVariables = extraSearchVariables,
            ).results
        } catch (e: Exception) {
            logger.error { "Error getting product acties: " + e.message }
        }
        return emptyList()
    }

    suspend fun getProductDocumentContent(
        authentication: CommonGroundAuthentication,
        productId: UUID,
        documentId: UUID,
    ): Pair<Document, Flow<DataBuffer>> {
        try {
            // get product, if not authorized throws an unauthorized
            val product =
                getProduct(
                    authentication = authentication,
                    id = productId,
                )

            // if product is not found, return a 204
            if (product == null) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")
            }

            val documentUrl =
                product.documenten.firstOrNull { extractId(it.url!!) == documentId }
                    ?: run {
                        logger.debug {
                            "Access denied to document $documentId on product $productId for user ${authentication.userId}"
                        }
                        throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied")
                    }
            val document =
                documentenApiService.getDocument(
                    documentUrl = documentUrl.url!!,
                )
            val content =
                documentenApiService.getDocumentContentStreaming(
                    informatieobejctUrl = documentUrl.url,
                )
            return document to content
        } catch (e: Exception) {
            logger.error(e) { "Error getting product document: " + e.message }
            if (e is NullPointerException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST)
            }
            throw e
        }
    }

    suspend fun getOpenProductDocumenten(
        openProductProduct: OpenProductProduct,
    ): List<Document> {
        val documents = mutableListOf<Document>()
        openProductProduct.documenten.forEach {
            try {
                documents.add(
                    documentenApiService
                        .getDocument(it.url!!),
                )
            } catch (e: Exception) {
                logger.error { "Error getting product document: ${e.message}" }
            }
        }
        return documentenApiService.filterDocuments(documents)
    }

    /**
     * Collect thema hierarchy up from subthema
     * @param: themaId, uuid of thema
     * @param: themas, all published themas
     * @return: list of themas
     */
    private fun collectThemaHierarchyUpFromSubThema(
        themaId: UUID,
        themas: List<OpenProductThema>? = emptyList(),
    ): List<OpenProductThema> {
        // 1. get themas, including the hoofdthema and may be their hoofdthema
        val collectedThemas = mutableSetOf<OpenProductThema>()

        val thema =
            themas?.find { it.uuid == themaId }

        if (thema == null) {
            return emptyList()
        } else {
            collectedThemas.add(thema)
        }

        // 1.5 get all the hoofdthema's
        collectedThemas.addAll(
            searchFromSubThemaUpToHoofdThema(
                thema = thema,
                themas = themas,
            ),
        )

        return collectedThemas.toList()
    }

    /**
     * Find taken by authentication, get the taken of the authenticated user
     * @param: productTypeId, uuid of producttype
     * @param: pageNumber, page number for pagination
     * @param: pageSize, page size for pagination
     * @return: list of taken
     */
    private suspend fun findTakenByIdentification(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
    ): List<TaakV2> {
        try {
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
                objectsApiTaskConfigProperties.objectTypeUrl,
                objectSearchParameters,
                pageNumber,
                pageSize,
            ).let { resultPage ->
                TaakPageV2.fromResultPage(pageNumber, pageSize, resultPage)
            }.content
        } catch (e: Exception) {
            logger.error { "Error getting thema taken: " + e.message }
        }
        return emptyList()
    }

    /**
     * Get objecten from Objects API
     * @param: objectTypeUrl, url of the objecttype
     * @param: searchParameters, the query search parameter
     * @param: pageNumber, page number for pagination
     * @param: pageSize, page size for pagination
     * @return: Results of objects
     */
    private suspend inline fun <reified T> getObjectsApiObjectResultPage(
        objectTypeUrl: String,
        searchParameters: List<ObjectSearchParameter>,
        pageNumber: Int,
        pageSize: Int,
    ): nl.nlportal.zgw.objectenapi.domain.ResultPage<ObjectsApiObject<T>> =
        objectsApiClient.getObjects<T>(
            objectSearchParameters = searchParameters,
            objectTypeUrl = objectTypeUrl,
            page = pageNumber,
            pageSize = pageSize,
            ordering = "-record__startAt",
        )

    /**
     * Search themas from subthema all the way up to the hoofd thema
     * @param: thema
     * @param: themas, all published themas
     * @return: Results of themas
     */
    private fun searchFromSubThemaUpToHoofdThema(
        thema: OpenProductThema,
        themas: List<OpenProductThema>? = emptyList(),
    ): List<OpenProductThema> {
        val hoofdThemas = mutableListOf<OpenProductThema>()
        if (thema.hoofdThema != null) {
            val hoofdThema =
                themas?.find { it.uuid == thema.hoofdThema }
            if (hoofdThema != null) {
                hoofdThemas.add(hoofdThema)
                hoofdThemas.addAll(
                    searchFromSubThemaUpToHoofdThema(
                        thema = hoofdThema,
                        themas = themas,
                    ),
                )
            }
        }

        return hoofdThemas
    }

    /**
     * Get the eigenaar filter used for producten
     * @param: authentication, authenticated user
     * @param: themas, all published themas
     * @return: list of filters
     */
    private fun getEigenaarFilter(authentication: CommonGroundAuthentication): List<Pair<OpenProductProductenFilters, String>> =
        when (authentication) {
            is BurgerAuthentication -> {
                listOf(
                    Pair(
                        OpenProductProductenFilters.EIGENAREN_BSN,
                        authentication.userId,
                    ),
                )
            }

            is BedrijfAuthentication -> {
                val vestigingsNummer = authentication.getVestigingsNummer()
                if (vestigingsNummer != null) {
                    listOf(
                        Pair(
                            OpenProductProductenFilters.EIGENAREN_VESTIGINGSNUMMER,
                            vestigingsNummer,
                        ),
                    )
                } else {
                    listOf(
                        Pair(
                            OpenProductProductenFilters.EIGENAREN_KVKNUMMER,
                            authentication.userId,
                        ),
                    )
                }
            }

            else -> {
                throw IllegalArgumentException("Authentication not supported")
            }
        }

    /**
     * Is user authorized to view the product
     * @param: authentication, authenticated user
     * @param: product, the product to be checked
     * @return: true or fale
     */
    private fun isAuthorizedForProduct(
        authentication: CommonGroundAuthentication,
        product: OpenProductProduct,
    ): Boolean =
        when (authentication) {
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

            else -> {
                false
            }
        }

    /**
     * Build a thema hierarchy from a specific thema
     * @param: thema
     * @return: list of thema hierarchy
     */
    private suspend fun buildThemaHierachy(thema: OpenProductThema): List<OpenProductThemaHierarchy> {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()
        val themas = getThemas(1, 999).results

        themasHierarchy.add(
            searchSubThemasHierarchy(
                thema = thema,
                themas = themas,
            ),
        )

        return themasHierarchy
    }

    /**
     * Search
     * @param: thema
     * @param: themas
     * @return: thema hierarchy
     */
    private fun searchSubThemasHierarchy(
        thema: OpenProductThema,
        themas: List<OpenProductThema>,
    ): OpenProductThemaHierarchy {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()

        val subThemas =
            themas.toList().filter { it.hoofdThema == thema.uuid }

        subThemas.forEach {
            themasHierarchy.add(
                searchSubThemasHierarchy(
                    thema = it,
                    themas = themas,
                ),
            )
        }

        return OpenProductThemaHierarchy(
            thema = thema,
            subThemas = themasHierarchy,
        )
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}