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
package nl.nlportal.product.graphql

import tools.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.WithBedrijfUser
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.product.TestHelper
import nl.nlportal.zakenapi.client.ZakenApiConfig
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.URI
import nl.nlportal.core.util.Mapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class ProductQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val zakenApiConfig: ZakenApiConfig,
    @Autowired private val graphqlGetProduct: String,
    @Autowired private val graphqlGetProducten: String,
    @Autowired private val graphqlGetProductZaken: String,
    @Autowired private val graphqlGetProductZakenNoZaakTypes: String,
    @Autowired private val graphqlGetProductTaken: String,
    @Autowired private val graphqlGetProductZakenNotFound: String,
    @Autowired private val graphqlGetProductVerbruiksObjecten: String,
    @Autowired private val graphqlGetProductType: String,
    @Autowired private val graphqlGetProductTypes: String,
    @Autowired private val graphqlGetProductDecision: String,
    @Autowired private val graphqlGetDecision: String,
    @Autowired private val graphqlProductPrefill: String,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.zakenapi.properties.url") { url }
            propsRegistry.add("nl-portal.config.dmn.properties.url") { url }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start()
            url = server?.url("/").toString()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            server?.shutdown()
        }
    }

    @BeforeEach
    internal fun setUp() {
        setupMockServer()
        url = server?.url("/").toString()
        objectsApiClientConfig.properties.url = URI(url)
        zakenApiConfig.properties.url = url
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductenTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProducten)
                .execute()
                .errors()
                .verify()
                .path("getProducten")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("2d725c07-2f26-4705-8637-438a42b5ac2d", responseBody.requiredAt("/content/0/id")?.stringValue())
        assertEquals("erfpacht", responseBody.requiredAt("/content/0/naam")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProduct)
                .execute()
                .errors()
                .verify()
                .path("getProduct")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("erfpacht", responseBody.requiredAt("/naam")?.stringValue())
        assertEquals("7d9cd6c2-8147-46f2-9ae9-c67e8213c202", responseBody.requiredAt("/zaken/0/uuid")?.stringValue())
        assertEquals("Lopende zaak", responseBody.requiredAt("/zaken/0/omschrijving")?.stringValue())
        assertEquals("Taak linked to Zaak", responseBody.requiredAt("/taken/0/titel")?.stringValue())
        assertEquals("7d9cd6c2-8147-46f2-9ae9-c67e8213c500", responseBody.requiredAt("/productDetails/id")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductZakenTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductZaken)
                .execute()
                .errors()
                .verify()
                .path("getProductZaken")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("Lopende zaak", responseBody.requiredAt("/0/omschrijving")?.stringValue())
    }

    @Test
    @WithBedrijfUser(
        kvkNummer = "569312863",
        machtigingsDienst = "dd95bdee-c493-4757-bae3-fe0a5b5063f8",
    )
    fun getProductZakenTestBedrijf() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductZaken)
                .execute()
                .errors()
                .verify()
                .path("getProductZaken")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("Lopende zaak", responseBody.requiredAt("/0/omschrijving")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductVerbruiksObjectenTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductVerbruiksObjecten)
                .execute()
                .errors()
                .verify()
                .path("getProductVerbruiksObjecten")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("2d725c07-2f26-4705-8637-438a42b5a800", responseBody.requiredAt("/0/id")?.stringValue())
        assertEquals("test verbruiksobject", responseBody.requiredAt("/0/soort")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTypeTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductType)
                .execute()
                .errors()
                .verify()
                .path("getProductType")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("7d9cd6c2-8147-46f2-9ae9-c67e8213c200", responseBody.requiredAt("/id")?.stringValue())
        assertEquals("erfpacht", responseBody.requiredAt("/naam")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTypesTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductTypes)
                .execute()
                .errors()
                .verify()
                .path("getProductTypes")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(1, responseBody.size())
        assertEquals("7d9cd6c2-8147-46f2-9ae9-c67e8213c200", responseBody.requiredAt("/0/id")?.stringValue())
        assertEquals("erfpacht", responseBody.requiredAt("/0/naam")?.stringValue())
    }

    @Test
    @WithBedrijfUser(
        kvkNummer = "569312863",
        machtigingsDienst = "dd95bdee-c493-4757-bae3-fe0a5b5063f8",
    )
    fun getProductTypesTestBedrijfWithMachtingDienst() {
        val basePath = "$.data.getProductTypes"

        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductTypes)
                .execute()
                .errors()
                .verify()
                .path("getProductTypes")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(0, responseBody.size())
    }

    @Test
    @WithBurgerUser("569312863")
    fun getProductTakenTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductTaken)
                .execute()
                .errors()
                .verify()
                .path("getProductTaken")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(2, responseBody.size())
        assertEquals("2d725c07-2f26-4705-8637-438a42b5ac2d", responseBody.requiredAt("/0/id")?.stringValue())
        assertEquals("Taak linked to Zaak", responseBody.requiredAt("/0/titel")?.stringValue())
    }

    @Test
    @WithBedrijfUser(
        kvkNummer = "569312863",
        machtigingsDienst = "dd95bdee-c493-4757-bae3-fe0a5b5063f8",
    )
    fun getProductTakenTestBedrijf() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductTaken)
                .execute()
                .errors()
                .verify()
                .path("getProductTaken")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(2, responseBody.size())
        assertEquals("2d725c07-2f26-4705-8637-438a42b5ac2d", responseBody.requiredAt("/0/id")?.stringValue())
        assertEquals("Taak linked to Zaak", responseBody.requiredAt("/0/titel")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getProductTakenTestBurgerNoTaken() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductTaken)
                .execute()
                .errors()
                .verify()
                .path("getProductTaken")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(0, responseBody.size())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getProductDescisionTest() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetProductDecision)
                .execute()
                .errors()
                .verify()
                .path("getProductDecision")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("https://formulier.denhaag.nl/Tripleforms/formulier/nl-NL/DefaultEnvironment/scNaheffingsAanslagParkeren.aspx", responseBody.requiredAt("/0/action/value")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getDescisionTest() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetDecision)
                .execute()
                .errors()
                .verify()
                .path("getDecision")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("https://formulier.denhaag.nl/Tripleforms/formulier/nl-NL/DefaultEnvironment/scNaheffingsAanslagParkeren.aspx", responseBody.requiredAt("/0/action/value")?.stringValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun productPrefillTestBurger() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlProductPrefill)
                .execute()
                .errors()
                .verify()
                .path("productPrefill")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("f9d7f166-bcea-4448-a984-4e717e558458", responseBody.requiredAt("/objectId")?.stringValue())
        assertEquals("http://localhost:8080/formuliernaam", responseBody.requiredAt("/formulierUrl")?.stringValue())
    }

    fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                    val response =
                        when (request.method + " " + path) {
                            "GET /api/v2/objects" -> {
                                if (queryParams.any { it.contains("identificatie__value__exact__569312863") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-taken.json")
                                } else if (queryParams.any { it.contains("identificatie__value__exact__569312864") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-taken-empty.json")
                                } else if (queryParams.any { it.contains("rollen__initiator__identificatie__exact__569312863") } &&
                                    queryParams.any { it.contains("PDCProductType__exact__7d9cd6c2-8147-46f2-9ae9-c67e8213c200") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-producten.json")
                                } else if (queryParams.any { it.contains("naam__exact__erfpacht") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-types.json")
                                } else if (queryParams.any { it.contains("naam__exact__noZaakTypes") }) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-types-no-zaaktypes.json")
                                } else if (queryParams.any {
                                        it.contains(
                                            "type=http://host.docker.internal:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb35",
                                        )
                                    }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-types-list.json")
                                } else if (queryParams.any {
                                        it.contains(
                                            "type=http://host.docker.internal:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb37",
                                        )
                                    } &&
                                    queryParams.any { it.contains("productInstantie__exact__7d9cd6c2-8147-46f2-9ae9-c67e8213c500") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-objecten.json")
                                } else if (queryParams.any {
                                        it.contains(
                                            "type=http://host.docker.internal:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb37",
                                        )
                                    } &&
                                    queryParams.any { it.contains("productInstantie__exact__2d725c07-2f26-4705-8637-438a42b5ac2d") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-objecten.json")
                                } else if (queryParams.any {
                                        it.contains(
                                            "type=http://host.docker.internal:8011/api/v1/objecttypes/3e852115-277a-4570-873a-9a64be3aeb36",
                                        )
                                    } &&
                                    queryParams.any {
                                        it.contains(
                                            "productInstantie__exact__2d725c07-2f26-4705-8637-438a42b5ac2d",
                                        )
                                    }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-product-details.json")
                                } else if (queryParams.any { it.contains("identificatie__exact__569312863") } &&
                                    queryParams.any { it.contains("formulier__exact__watkanikregelen") }
                                ) {
                                    TestHelper.mockResponseFromFile("/product/data/get-prefill-objecten.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }

                            "POST /zaken/api/v1/zaken/_zoek" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-zaken.json")
                            }

                            "POST /api/v2/objects" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-prefill-object.json")
                            }

                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5a800" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-verbruiks-object.json")
                            }

                            "GET /api/v2/objects/7d9cd6c2-8147-46f2-9ae9-c67e8213c200" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product-type.json")
                            }

                            "GET /zaken/api/v1/zaken/7d9cd6c2-8147-46f2-9ae9-c67e8213c202" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-zaak.json")
                            }

                            "GET /api/v2/objects/58fad5ab-dc2f-11ec-9075-f22a405ce708" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-taak.json")
                            }

                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5ac2d" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-product.json")
                            }

                            "POST /decision-definition/key/watkanikregelenDmn/evaluate" -> {
                                TestHelper.mockResponseFromFile("/product/data/get-dmn-decision.json")
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}