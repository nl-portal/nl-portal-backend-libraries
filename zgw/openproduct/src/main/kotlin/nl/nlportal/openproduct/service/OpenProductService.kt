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
            logger.error { "Error getting themas with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getHoofdThemas(): List<OpenProductThema> = getThemas(1, 999).resultaten.filter { it.hoofdThema == null }

    suspend fun getThemasHierarchy(): List<OpenProductThemaHierarchy> {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()
        val themas = getThemas(1, 999).resultaten
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

    suspend fun getThema(id: UUID): OpenProductThema? {
        try {
            return openProductTypeClient.path<Themas>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting thema with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error building thema hierarchy id: $id with cause: $e.message" }
        }
        return emptyList()
    }

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
            logger.error { "Error getting productTypes with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

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
            logger.error { "Error getting producttype with id: $id with cause: $e.message" }
        }
        return null
    }

    suspend fun getActies(
        pageNumber: Int,
        pageSize: Int,
        naam: String? = null,
        extraSearchVariables: List<Pair<OpenProductActiesFilters, Any>> = emptyList(),
    ): ResultPage<OpenProductActie> {
        try {
            val searchVariables =
                mutableListOf<Pair<OpenProductActiesFilters, Any>>(
                    OpenProductActiesFilters.PAGE to pageNumber.toString(),
                    OpenProductActiesFilters.PAGE_SIZE to pageSize.toString(),
                )

            naam?.let {
                searchVariables.add(OpenProductActiesFilters.NAAM_CONTAINS to it)
            }

            if (extraSearchVariables.isNotEmpty()) {
                searchVariables.addAll(extraSearchVariables)
            }
            return openProductTypeClient.path<Acties>().get(searchVariables)
        } catch (e: Exception) {
            logger.error { "Error getting acties with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getActie(id: UUID): OpenProductActie? {
        try {
            return openProductTypeClient.path<Acties>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting actie with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting bestanden with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getBestand(id: UUID): OpenProductBestand? {
        try {
            return openProductTypeClient.path<Bestanden>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting bestand with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting contacten with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getContact(id: UUID): OpenProductContact? {
        try {
            return openProductTypeClient.path<Contacten>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting contact with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting locaties with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getLocatie(id: UUID): OpenProductLocatie? {
        try {
            return openProductTypeClient.path<Locaties>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting locatie with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting organisaties with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getOrganisatie(id: UUID): OpenProductOrganisatie? {
        try {
            return openProductTypeClient.path<Organisaties>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting organisatie with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting prijzen with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getPrijs(id: UUID): OpenProductPrijs? {
        try {
            return openProductTypeClient.path<Prijzen>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting prijs with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting schemas with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getSchema(id: UUID): OpenProductSchema? {
        try {
            return openProductTypeClient.path<Schemas>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting schema with id: $id with cause: $e.message" }
        }
        return null
    }

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
            logger.error { "Error getting links with cause: $e.message" }
        }

        return ResultPage(
            aantal = 0,
            resultaten = emptyList(),
        )
    }

    suspend fun getLink(id: UUID): OpenProductLink? {
        try {
            return openProductTypeClient.path<Links>().get(
                id = id,
            )
        } catch (e: Exception) {
            logger.error { "Error getting link with id: $id with cause: $e.message" }
        }
        return null
    }

    suspend fun getProductTypeContent(productTypeId: UUID): List<OpenProductProductTypeContent>? {
        try {
            return openProductTypeClient.path<ProductTypes>().get(
                id = productTypeId,
            )
        } catch (e: Exception) {
            logger.error { "Error getting producttype content with id: $productTypeId with cause: $e.message" }
        }
        return null
    }

    suspend fun getProducten(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
        extraSearchVariables: List<Pair<OpenProductProductenFilters, String>> = emptyList(),
        status: OpenProductToegestaneStatus? = null,
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

        if (status != null) {
            searchVariables.add(OpenProductProductenFilters.STATUS to status.toString().lowercase())
        }

        return openProductClient.path<Producten>().get(
            searchFilters = searchVariables,
        )
    }

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
                logger.error { "Error getting product with id: $id with cause: $e.message" }
            }
        }
        return null
    }

    suspend fun getProductenByThema(
        authentication: CommonGroundAuthentication,
        themaId: UUID,
    ): List<OpenProductProduct> {
        try {
            val thema = getThema(id = themaId)
            if (thema == null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not find thema with id: $themaId")
            }

            val searchVariables =
                listOf(
                    OpenProductProductenFilters.THEMA_UUID to thema.uuid.toString(),
                )
            return getProducten(
                authentication = authentication,
                pageNumber = 1,
                pageSize = 999,
                extraSearchVariables = searchVariables,
            ).resultaten
        } catch (e: Exception) {
            logger.error { "Error getting products by thema id: $themaId with cause: $e.message" }
        }

        return emptyList()
    }

    suspend fun updateProduct(
        authentication: CommonGroundAuthentication,
        productUpdate: OpenProductProductUpdate,
    ): OpenProductProduct? {
        // get product, only to check if user authorized to perform this update
        val product =
            getProduct(
                authentication = authentication,
                id = productUpdate.uuid,
            )

        return openProductClient.path<Producten>().patch(productUpdate = productUpdate)
    }

    suspend fun getThemaZaken(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
        isOpen: Boolean? = null,
        id: UUID,
        language: String? = null,
        themasList: List<OpenProductThema>? = null,
    ): List<Zaak> {
        val themas = themasList ?: collectThemaHierarchyUpFromSubThema(id)

        // 2. loop through productTypes and get zaakTypes
        val zaakTypes = mutableListOf<UUID>()

        themas.forEach { thema ->
            thema.producttypen.forEach {
                getProductType(
                    id = it.uuid,
                    language = language,
                )?.zaaktypen?.forEach { zaakType ->
                    zaakTypes.add(CoreUtils.extractId(zaakType.url))
                }
            }
        }

        // 3. get Zaken with filters
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

    suspend fun getThemaTaken(
        authentication: CommonGroundAuthentication,
        pageNumber: Int,
        pageSize: Int,
        id: UUID,
        language: String? = null,
    ): List<TaakV2> {
        val themas = collectThemaHierarchyUpFromSubThema(id)
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
                id = id,
                language = language,
                themasList = themas,
            )

        val searchVariables =
            listOf(
                OpenProductProductenFilters.THEMA_UUID_IN to themas.joinToString { it.uuid.toString() },
            )

        val producten =
            getProducten(
                authentication = authentication,
                pageNumber = pageNumber,
                pageSize = pageSize,
                extraSearchVariables = searchVariables,
            ).resultaten

        // filter out the taak which is not connected to a zaak
        return taken
            .filterNot { task ->
                !zaken.any { it.uuid.toString() == task.koppeling.value } &&
                    !producten.any { it.uuid.toString() == task.koppeling.value }
            }.sortedBy { it.verloopdatum }
    }

    suspend fun getProductZaken(zaken: List<OpenProductUrl>): List<Zaak> =
        zaken.map {
            zakenApiClient.zaken().get(CoreUtils.extractId(it.url)).retrieve()
        }

    suspend fun getProductTaken(
        taken: List<OpenProductUrl>,
    ): List<TaakV2> {
        try {
            val takenList = mutableListOf<TaakV2>()

            taken.forEach {
                val taakObject = objectsApiClient.getObjectById<TaakV2>(CoreUtils.extractId(it.url).toString())
                if (taakObject != null) {
                    takenList.add(taakObject.record.data)
                }
            }

            return takenList
        } catch (e: Exception) {
            logger.error { "Error getting product taken: $e.message" }
        }
        return emptyList()
    }

    private suspend fun collectThemaHierarchyUpFromSubThema(id: UUID): List<OpenProductThema> {
        // 1. get themas, including the hoofdthema and may be their hoofdthema
        val themas = mutableSetOf<OpenProductThema>()

        val thema =
            getThema(
                id = id,
            )

        if (thema == null) {
            return emptyList()
        } else {
            themas.add(thema)
        }

        // 1.5 get all the hoofdthema's
        themas.addAll(searchFromSubThemaUpToHoofdThema(thema))

        return themas.toList()
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
        }.content
    }

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

    private suspend fun searchFromSubThemaUpToHoofdThema(thema: OpenProductThema): List<OpenProductThema> {
        val hoofdThemas = mutableListOf<OpenProductThema>()
        if (thema.hoofdThema != null) {
            val hoofdThema =
                getThema(
                    id = thema.hoofdThema,
                )
            if (hoofdThema != null) {
                hoofdThemas.add(hoofdThema)
                hoofdThemas.addAll(searchFromSubThemaUpToHoofdThema(hoofdThema))
            }
        }

        return hoofdThemas
    }

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

            else -> false
        }

    private suspend fun buildThemaHierachy(thema: OpenProductThema): List<OpenProductThemaHierarchy> {
        val themasHierarchy = mutableListOf<OpenProductThemaHierarchy>()
        val themas = getThemas(1, 999).resultaten

        themasHierarchy.add(
            searchSubThemasHierarchy(
                thema = thema,
                themas = themas,
            ),
        )

        return themasHierarchy
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}