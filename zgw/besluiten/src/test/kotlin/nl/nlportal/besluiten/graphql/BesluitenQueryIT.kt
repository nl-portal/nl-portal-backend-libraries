package nl.nlportal.besluiten.graphql

import nl.nlportal.besluiten.TestHelper
import nl.nlportal.besluiten.TestHelper.verifyOnlyDataExists
import nl.nlportal.besluiten.client.BesluitenApiConfig
import nl.nlportal.commonground.authentication.WithBurgerUser
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_METHOD)
class BesluitenQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val besluitenApiConfig: BesluitenApiConfig,
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
            propsRegistry.add("nl-portal.zgw.besluiten.url") { url }
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
        besluitenApiConfig.url = url
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluiten() {
        val basePath = "$.data.getBesluiten"
        val resultPath = "$basePath.content[0]"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetBesluiten)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$resultPath.identificatie").isEqualTo("klantportaal")
            .jsonPath("$resultPath.toelichting").isEqualTo("toelichting")
            .jsonPath("$resultPath.publicatiedatum").isEqualTo("2019-08-24")
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluit() {
        val basePath = "$.data.getBesluit"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetBesluit)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.identificatie").isEqualTo("klantportaal")
            .jsonPath("$basePath.toelichting").isEqualTo("toelichting")
            .jsonPath("$basePath.publicatiedatum").isEqualTo("2019-08-24")
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitAuditTrails() {
        val basePath = "$.data.getBesluitAuditTrails"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetBesluitAuditTrails)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath[0].uuid").isEqualTo("095be615-a8ad-4c33-8e9c-c7612fbf6c9f")
            .jsonPath("$basePath[0].bron").isEqualTo("ac")
            .jsonPath("$basePath[0].actie").isEqualTo("list")
            .jsonPath("$basePath[0].aanmaakdatum").isEqualTo("2019-08-24T14:15:22")
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitAuditTrail() {
        val basePath = "$.data.getBesluitAuditTrail"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetBesluitAuditTrail)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.uuid").isEqualTo("095be615-a8ad-4c33-8e9c-c7612fbf6c9f")
            .jsonPath("$basePath.bron").isEqualTo("ac")
            .jsonPath("$basePath.actie").isEqualTo("list")
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitDocumenten() {
        val basePath = "$.data.getBesluitDocumenten"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetBesluitDocumenten)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath[0].url").isEqualTo("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z")
            .jsonPath(
                "$basePath[0].informatieobject",
            ).isEqualTo("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z")
            .jsonPath(
                "$basePath[0].besluit",
            ).isEqualTo("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z")
    }

    @Test
    @WithBurgerUser("569312864")
    fun getBesluitDocument() {
        val basePath = "$.data.getBesluitDocument"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(graphqlGetBesluitDocument)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.url").isEqualTo("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z")
            .jsonPath(
                "$basePath.informatieobject",
            ).isEqualTo("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z")
            .jsonPath(
                "$basePath.besluit",
            ).isEqualTo("http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z")
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
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}