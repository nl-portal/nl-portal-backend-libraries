package nl.nlportal.besluiten.graphql

import tools.jackson.databind.JsonNode
import nl.nlportal.besluiten.TestHelper
import nl.nlportal.besluiten.client.BesluitenApiConfig
import nl.nlportal.catalogiapi.client.CatalogiApiConfig
import nl.nlportal.commonground.authentication.WithBurgerUser
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_METHOD)
class BesluitenQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val besluitenApiConfig: BesluitenApiConfig,
    @Autowired private val catalogiApiConfig: CatalogiApiConfig,
    @Autowired private val graphqlGetBesluiten: String,
    @Autowired private val graphqlGetBesluit: String,
    @Autowired private val graphqlGetBesluitAuditTrails: String,
    @Autowired private val graphqlGetBesluitAuditTrail: String,
    @Autowired private val graphqlGetBesluitDocumenten: String,
    @Autowired private val graphqlGetBesluitDocument: String,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.besluitenapi.properties.url") { url }
            propsRegistry.add("nl-portal.config.catalogiapi.properties.url") { url }
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
        setupMockObjectsApiServer()
        url = server?.url("/").toString()
        besluitenApiConfig.properties.url = url
        catalogiApiConfig.properties.url = url
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluiten() {

        val responseBody =
            httpGraphQlTester
                .document(graphqlGetBesluiten)
                .execute()
                .errors()
                .verify()
                .path("getBesluiten")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("klantportaal", responseBody.requiredAt("/content/0/identificatie").stringValue())
        assertEquals("toelichting", responseBody.requiredAt("/content/0/toelichting").stringValue())
        assertEquals("2019-08-24", responseBody.requiredAt("/content/0/publicatiedatum").stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluit() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetBesluit)
                .execute()
                .errors()
                .verify()
                .path("getBesluit")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("klantportaal", responseBody.get("identificatie").stringValue())
        assertEquals("toelichting", responseBody.get("toelichting").stringValue())
        assertEquals("2019-08-24", responseBody.get("publicatiedatum").stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitAuditTrails() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetBesluitAuditTrails)
                .execute()
                .errors()
                .verify()
                .path("getBesluitAuditTrails")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("095be615-a8ad-4c33-8e9c-c7612fbf6c9f", responseBody.requiredAt("/0/uuid").stringValue())
        assertEquals("ac", responseBody.requiredAt("/0/bron").stringValue())
        assertEquals("list", responseBody.requiredAt("/0/actie").stringValue())
        assertEquals("2019-08-24T14:15:22", responseBody.requiredAt("/0/aanmaakdatum").stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitAuditTrail() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetBesluitAuditTrail)
                .execute()
                .errors()
                .verify()
                .path("getBesluitAuditTrail")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("095be615-a8ad-4c33-8e9c-c7612fbf6c9f", responseBody.get("uuid").stringValue())
        assertEquals("ac", responseBody.get("bron").stringValue())
        assertEquals("list", responseBody.get("actie").stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitDocumenten() {
        val responseBody =
        httpGraphQlTester
            .document(graphqlGetBesluitDocumenten)
            .execute()
            .errors()
            .verify()
            .path("getBesluitDocumenten")
            .entity(JsonNode::class.java)
            .get()

        assertEquals("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z", responseBody.requiredAt("/0/url").stringValue())
        assertEquals("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z", responseBody.requiredAt("/0/informatieobject").stringValue())
        assertEquals("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z", responseBody.requiredAt("/0/besluit").stringValue())
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitDocument() {
        val responseBody =
            httpGraphQlTester
                .document(graphqlGetBesluitDocument)
                .execute()
                .errors()
                .verify()
                .path("getBesluitDocument")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z", responseBody.get("url").stringValue())
        assertEquals("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z", responseBody.get("informatieobject").stringValue())
        assertEquals("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z", responseBody.get("besluit").stringValue())
    }

    fun setupMockObjectsApiServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                    val response =
                        when (request.method + " " + path) {
                            "GET /besluiten/api/v1/besluiten/58fad5ab-dc2f-11ec-9075-f22a405ce707" -> {
                                TestHelper.mockResponseFromFile("/data/get-besluit.json")
                            }
                            "GET /besluiten/api/v1/besluiten/58fad5ab-dc2f-11ec-9075-f22a405ce707/audittrail" -> {
                                TestHelper.mockResponseFromFile("/data/get-audittrails.json")
                            }
                            "GET /besluiten/api/v1/besluiten/58fad5ab-dc2f-11ec-9075-f22a405ce707/audittrail/095be615-a8ad-4c33-8e9c-c7612fbf6c9f" -> {
                                TestHelper.mockResponseFromFile("/data/get-audittrail.json")
                            }
                            "GET /besluiten/api/v1/besluitinformatieobjecten" -> {
                                TestHelper.mockResponseFromFile("/data/get-documenten.json")
                            }
                            "GET /besluiten/api/v1/besluitinformatieobjecten/6a337d56-5a00-427f-8a15-21aa977b2512" -> {
                                TestHelper.mockResponseFromFile("/data/get-document.json")
                            }
                            "GET /besluiten/api/v1/besluiten" -> {
                                TestHelper.mockResponseFromFile("/data/get-besluiten.json")
                            }
                            "GET /catalogi/api/v1/besluittypen/496f51fd-ccdb-406e-805a-e7602ae78a2b" -> {
                                TestHelper.mockResponseFromFile("/data/get-besluittype.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}
