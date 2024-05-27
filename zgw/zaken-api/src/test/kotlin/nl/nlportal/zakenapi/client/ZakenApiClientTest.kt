package nl.nlportal.zakenapi.client

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.JwtBuilder
import nl.nlportal.zakenapi.client.request.ObjectType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
class ZakenApiClientTest {
    lateinit var zakenApiConfig: ZakenApiConfig
    lateinit var host: String
    lateinit var client: WebClient
    lateinit var mockServer: MockWebServer
    lateinit var zakenApiClient: ZakenApiClient

    @BeforeEach
    fun setUp() {
        mockServer = MockWebServer()
        mockServer.start()

        host = "http://${mockServer.hostName}:${mockServer.port}"
        client = WebClient.builder().baseUrl(host).build()

        zakenApiConfig =
            ZakenApiConfig(
                host,
                "gzac",
                "12345678123456781234567812345678",
            )

        zakenApiClient = ZakenApiClient(zakenApiConfig, WebClient.builder())
    }

    @AfterEach
    internal fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `zaak url - generated correctly`() {
        val value = zakenApiClient.getZaakUrl(UUID.fromString("2941048a-7290-4a78-8c4e-14feaec137dc"))

        assertEquals("$host/zaken/api/v1/zaken/2941048a-7290-4a78-8c4e-14feaec137dc", value)
    }

    @Test
    fun `zaken search - errors`() {
        mockServer.enqueue(MockResponse().setResponseCode(404))

        return runTest {
            assertFailsWith<ResponseStatusException> {
                zakenApiClient.zaken().search().retrieve()
            }
        }
    }

    @Test
    fun `zaken search - verify headers`() {
        mockServer.enqueue(emptyZakenResponse())

        return runTest {
            zakenApiClient.zaken().search().retrieve()

            val request = mockServer.takeRequest()
            assertEquals("GET", request.method)
            assertEquals("Bearer ", request.getHeader("Authorization")?.substring(0, 7))
            assertEquals("EPSG:4326", request.getHeader("Accept-Crs"))
            assertEquals("EPSG:4326", request.getHeader("Content-Crs"))
        }
    }

    @Test
    fun `zaken search - verify query parameters`() {
        mockServer.enqueue(emptyZakenResponse())

        return runTest {
            zakenApiClient
                .zaken()
                .search()
                .ofZaakType("https://localhost:1000/type")
                .withBsn("BSN")
                .withKvk("KVK")
                .withUid("UID")
                .retrieve()

            val request = mockServer.takeRequest()
            assertEquals(
                "BSN",
                request.requestUrl?.queryParameter("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn"),
            )
            assertEquals(
                "KVK",
                request.requestUrl?.queryParameter("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie"),
            )
            assertEquals(
                "UID",
                request.requestUrl?.queryParameter("rol__betrokkeneIdentificatie__natuurlijkPersoon__anpIdentificatie"),
            )
            assertEquals("https://localhost:1000/type", request.requestUrl?.queryParameter("zaaktype"))
        }
    }

    @Test
    fun `zaken search - verify query parameters for burger authentication`() {
        mockServer.enqueue(emptyZakenResponse())

        return runTest {
            zakenApiClient.zaken()
                .search()
                .withAuthentication(JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication())
                .retrieve()

            val request = mockServer.takeRequest()
            assertEquals(
                "123",
                request.requestUrl?.queryParameter("rol__betrokkeneIdentificatie__natuurlijkPersoon__inpBsn"),
            )
        }
    }

    @Test
    fun `zaken search - verify query parameters for bedrijf authentication`() {
        mockServer.enqueue(emptyZakenResponse())

        return runTest {
            zakenApiClient.zaken()
                .search()
                .withAuthentication(JwtBuilder().aanvragerKvk("123").buildBedrijfAuthentication())
                .retrieve()

            val request = mockServer.takeRequest()
            assertEquals(
                "123",
                request.requestUrl?.queryParameter("rol__betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie"),
            )
        }
    }

    @Test
    fun `zaken search - paging and retrieve all`() {
        pagedRetrieve(
            listOf(
                listOf(
                    zaak("1941048a-7290-4a78-8c4e-14feaec137dc"),
                ),
                listOf(
                    zaak("2941048a-7290-4a78-8c4e-14feaec137dc"),
                ),
            ),
        ).forEach { mockServer.enqueue(it) }

        return runTest {
            val response = zakenApiClient.zaken().search().retrieveAll()

            assertEquals(2, response.size)
            assertEquals(response[0].uuid, UUID.fromString("1941048a-7290-4a78-8c4e-14feaec137dc"))
            assertEquals(response[1].uuid, UUID.fromString("2941048a-7290-4a78-8c4e-14feaec137dc"))
        }
    }

    @Test
    fun `zaken get - get single zaak`() {
        mockServer.enqueue(
            emptyZakenResponse().setBody(
                zaak("2941048a-7290-4a78-8c4e-14feaec137dc"),
            ),
        )

        runTest {
            val response =
                zakenApiClient.zaken().get(UUID.fromString("2941048a-7290-4a78-8c4e-14feaec137dc")).retrieve()

            assertNotNull(response)
            assertEquals(response.uuid, UUID.fromString("2941048a-7290-4a78-8c4e-14feaec137dc"))

            val request = mockServer.takeRequest()
            assertEquals("/zaken/api/v1/zaken/2941048a-7290-4a78-8c4e-14feaec137dc", request.path)
        }
    }

    fun emptyZakenResponse(): MockResponse {
        return pagedRetrieve(listOf(listOf()))[0]
    }

    fun zaak(uuid: String = "1941048a-7290-4a78-8c4e-14feaec137dc"): String {
        return """
            {
                "uuid": "$uuid",
                "url": "http://localhost:1000",
                "identificatie": "ABC",
                "omschrijving": "ABCD",
                "zaaktype": "http://localhost:1000/type",
                "startdatum": "2024-10-10"
            }
            """.trimIndent()
    }

    @Test
    fun `search zakeninformatieobjecten - filters`() {
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    listOf(
                        zaakInformatieobjecten(),
                        zaakInformatieobjecten(),
                    ).joinToString(prefix = "[", postfix = "]"),
                )
                .addHeader("Content-Type", "application/json; charset=utf-8"),
        )

        return runTest {
            val response =
                zakenApiClient
                    .zaakInformatieobjecten()
                    .search()
                    .forZaak("http://localhost:1000/zaak")
                    .ofInformatieobject("http://localhost:1000/informatieobject")
                    .retrieve()

            assertEquals(2, response.size)
            assertEquals("1941048a-7290-4a78-8c4e-14feaec137dc", response[0].uuid)
            assertEquals("1941048a-7290-4a78-8c4e-14feaec137dc", response[1].uuid)

            val request = mockServer.takeRequest()
            assertEquals("http://localhost:1000/zaak", request.requestUrl?.queryParameter("zaak"))
            assertEquals(
                "http://localhost:1000/informatieobject",
                request.requestUrl?.queryParameter("informatieobject"),
            )
            assertEquals(2, request.requestUrl?.queryParameterNames?.size)
            assertEquals(true, request.path?.startsWith("/zaken/api/v1/zaakinformatieobjecten"))
        }
    }

    private fun zaakInformatieobjecten(uuid: String = "1941048a-7290-4a78-8c4e-14feaec137dc"): String {
        return """
            {
            "url": "http://example.com",
            "uuid": "$uuid",
            "informatieobject": "http://example.com",
            "zaak": "http://example.com",
            "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
            "titel": "string",
            "beschrijving": "string",
            "registratiedatum": "2019-08-24T14:15:22Z"
            }
            """.trimIndent()
    }

    @Test
    fun `search zaakobjecten - filters`() {
        pagedRetrieve(
            listOf(
                listOf(
                    zaakObjecten(),
                    zaakObjecten(),
                ),
            ),
        )
            .forEach { mockServer.enqueue(it) }

        return runTest {
            val response =
                zakenApiClient
                    .zaakObjecten()
                    .search()
                    .forZaak("http://localhost/zaak")
                    .ofObject("http://localhost/object").ofObjectType(ObjectType.WOZ_OBJECT)
                    .retrieveAll()

            assertEquals(2, response.size)
            assertEquals("095be615-a8ad-4c33-8e9c-c7612fbf6c9f", response[0].uuid)
            assertEquals("095be615-a8ad-4c33-8e9c-c7612fbf6c9f", response[1].uuid)

            val request = mockServer.takeRequest()
            assertEquals("http://localhost/zaak", request.requestUrl?.queryParameter("zaak"))
            assertEquals("http://localhost/object", request.requestUrl?.queryParameter("object"))
            assertEquals("woz_object", request.requestUrl?.queryParameter("objectType"))
            assertEquals(3, request.requestUrl?.queryParameterNames?.size)
            assertEquals(true, request.path?.startsWith("/zaken/api/v1/zaakobjecten"))
        }
    }

    fun zaakObjecten(uuid: String = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"): String {
        return """
            {
            "url": "http://example.com",
            "uuid": "$uuid",
            "zaak": "http://example.com",
            "object": "http://example.com",
            "objectType": "adres",
            "objectTypeOverige": "string",
            "relatieomschrijving": "string"
            }
            """.trimIndent()
    }

    @Test
    fun `search zaak rollen - filters`() {
        pagedRetrieve(
            listOf(
                listOf(
                    zaakRollen(),
                    zaakRollen(),
                ),
            ),
        ).forEach { mockServer.enqueue(it) }

        return runTest {
            val response =
                zakenApiClient
                    .zaakRollen()
                    .search()
                    .forZaak("http://localhost/zaak")
                    .withBsn("BSN")
                    .withKvk("KVK")
                    .withUid("UID")
                    .retrieveAll()

            assertEquals(2, response.size)
            assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), response[0].uuid)
            assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), response[1].uuid)

            val request = mockServer.takeRequest()
            assertEquals("http://localhost/zaak", request.requestUrl?.queryParameter("zaak"))
            assertEquals(
                "BSN",
                request.requestUrl?.queryParameter("betrokkeneIdentificatie__natuurlijkPersoon__inpBsn"),
            )
            assertEquals(
                "KVK",
                request.requestUrl?.queryParameter("betrokkeneIdentificatie__nietNatuurlijkPersoon__annIdentificatie"),
            )
            assertEquals(
                "UID",
                request.requestUrl?.queryParameter("betrokkeneIdentificatie__natuurlijkPersoon__anpIdentificatie"),
            )
            assertEquals(4, request.requestUrl?.queryParameterNames?.size)
            assertEquals(true, request.path?.startsWith("/zaken/api/v1/rollen"))
        }
    }

    fun zaakRollen(uuid: String = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"): String {
        return """
            {
            "url": "http://example.com",
            "uuid": "$uuid",
            "zaak": "http://example.com",
            "betrokkene": "http://example.com",
            "betrokkeneType": "natuurlijk_persoon",
            "roltype": "http://example.com",
            "omschrijving": "string",
            "omschrijvingGeneriek": "string",
            "roltoelichting": "string",
            "registratiedatum": "2019-08-24T14:15:22Z",
            "indicatieMachtiging": "gemachtigde"
            }
            """.trimIndent()
    }

    @Test
    fun `search zaak statussen - filters`() {
        pagedRetrieve(
            listOf(
                listOf(
                    zaakStatus(),
                    zaakStatus(),
                ),
            ),
        ).forEach {
            mockServer.enqueue(it)
        }

        return runTest {
            val response =
                zakenApiClient
                    .zaakStatussen()
                    .search()
                    .forZaak("http://localhost/zaak")
                    .forStatustype("status")
                    .retrieveAll()

            assertEquals(2, response.size)
            assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), response[0].uuid)
            assertEquals(UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f"), response[1].uuid)

            val request = mockServer.takeRequest()
            assertEquals("http://localhost/zaak", request.requestUrl?.queryParameter("zaak"))
            assertEquals("status", request.requestUrl?.queryParameter("statustype"))
            assertEquals(2, request.requestUrl?.queryParameterNames?.size)
            assertEquals(true, request.path?.startsWith("/zaken/api/v1/statussen"))
        }
    }

    fun zaakStatus(uuid: String = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"): String {
        return """
            {
            "url": "http://example.com",
            "uuid": "$uuid",
            "zaak": "http://example.com",
            "statustype": "http://example.com",
            "datumStatusGezet": "2019-08-24T14:15:22Z",
            "statustoelichting": "string"
            }
            """.trimIndent()
    }

    fun pagedRetrieve(pages: List<List<String>>): List<MockResponse> {
        val count = pages.fold(0) { acc, n -> acc + n.size }

        return pages
            .mapIndexed { index, results ->
                val next =
                    if (pages.size - 1 == index) {
                        "null"
                    } else {
                        "\"" + host + "?page=${index + 1}\""
                    }

                """
                {
                "count": "$count",
                "next": $next,
                "previous": null,
                "results": [${results.joinToString()}]
                }
                """.trimIndent()
            }.map {
                MockResponse()
                    .setResponseCode(200)
                    .setBody(it)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
            }
    }
}