package nl.nlportal.zakenapi.web.rest

import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.zakenapi.TestHelper
import nl.nlportal.zakenapi.client.ZakenApiConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ZaakDocumentResourceIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val zakenApiConfig: ZakenApiConfig,
) {
    lateinit var mockZakenApi: MockWebServer

    @BeforeEach
    fun setUp() {
        mockZakenApi = MockWebServer()
        mockZakenApi.start(8001)
        zakenApiConfig.url = mockZakenApi.url("/").toString()
    }

    @AfterAll
    internal fun tearDown() {
        mockZakenApi.shutdown()
    }

    @Test
    @WithBurgerUser("111111110")
    fun `should return empty response for incorrect user`() {
        // given
        val zaakDocumentResponse =
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(Mapper.get().writeValueAsString(TestHelper.testZaakDocument))
        val zaakRolResponse =
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "count": 0,
                        "results": []
                    }
                    """.trimIndent(),
                )
        val documentResponse =
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(Mapper.get().writeValueAsString(TestHelper.testDocument))
        val documentContentResponse =
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(getResourceContentByName("/config/data/example-text-file.txt")!!)

        with(mockZakenApi) {
            enqueue(zaakDocumentResponse)
            enqueue(zaakRolResponse)
            enqueue(documentResponse)
            enqueue(documentContentResponse)
        }

        // when
        webTestClient
            .get()
            .uri { builder ->
                builder
                    .path("/api/zakenapi/zaakdocument/${TestHelper.testZaakDocument.uuid}/content")
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith {
                assertNull(
                    it.responseBody,
                )
            }

        val zaakDocumentRequest = mockZakenApi.takeRequest()
        val zaakRolRequest = mockZakenApi.takeRequest()

        // then
        assertEquals(
            zaakDocumentRequest.requestUrl!!.encodedPath.substringAfterLast("/"),
            TestHelper.testZaakDocument.uuid,
        )
        assertEquals(
            zaakRolRequest.requestUrl!!.queryParameter("betrokkeneIdentificatie__natuurlijkPersoon__inpBsn"),
            "111111110",
        )
    }

    private fun getResourceContentByName(name: String): String? =
        this::class.java.getResourceAsStream(name)?.readAllBytes()?.decodeToString()
}