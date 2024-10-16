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
package nl.nlportal.zakenapi.graphql

import mu.KotlinLogging
import nl.nlportal.besluiten.client.BesluitenApiConfig
import nl.nlportal.catalogiapi.client.CatalogiApiConfig
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.zakenapi.client.ZakenApiConfig
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
import java.util.function.Consumer

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_METHOD)
internal class ZaakQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val zakenApiConfig: ZakenApiConfig,
    @Autowired private val catalogiApiConfig: CatalogiApiConfig,
    @Autowired private val documentApisConfig: DocumentApisConfig,
    @Autowired private val besluitenApiConfig: BesluitenApiConfig,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.zgw.zakenapi.url") { url }
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
        setupMockOpenZaakServer()
        url = server?.url("/").toString()

        zakenApiConfig.url = url
        catalogiApiConfig.url = url
        besluitenApiConfig.url = url
        documentApisConfig.getConfig("openzaak").url = url
    }

    @Test
    @WithBurgerUser("123")
    fun getZaken() {
        val query =
            """
            query {
                getZaken(page: 1) {
                number
                size
                totalPages
                totalElements
                numberOfElements
                content {
                        uuid,
                        identificatie,
                        omschrijving,
                        zaaktype {
                            identificatie,
                            omschrijving
                        },
                        startdatum,
                        status {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        },
                        statusGeschiedenis {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        }
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getZaken"
        val resultPath = "$basePath.content[0]"

        val response =
            testClient.post()
                .uri("/graphql")
                .accept(APPLICATION_JSON)
                .contentType(MediaType("application", "graphql"))
                .bodyValue(query)
                .exchange()
                .expectBody()
                .consumeWith(Consumer { t -> logger.info { t } })

        response
            .jsonPath(basePath).exists()
            .jsonPath("$resultPath.uuid").isEqualTo("5d479908-fbb7-49c2-98c9-9afecf8de79a")
            .jsonPath("$resultPath.identificatie").isEqualTo("ZAAK-2021-0000000003")
            .jsonPath("$resultPath.omschrijving").isEqualTo("Voorbeeld afgesloten zaak 1")
            .jsonPath("$resultPath.startdatum").isEqualTo("2021-09-16")
            .jsonPath("$resultPath.zaaktype.identificatie").isEqualTo("bezwaar-behandelen")
            .jsonPath("$resultPath.zaaktype.omschrijving").isEqualTo("Bezwaar behandelen")
            .jsonPath("$resultPath.status.datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$resultPath.status.statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$resultPath.status.statustype.isEindstatus").isEqualTo(true)
            .jsonPath("$resultPath.statusGeschiedenis[0].datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$resultPath.statusGeschiedenis[0].statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$resultPath.statusGeschiedenis[0].statustype.isEindstatus").isEqualTo(true)
    }

    @Test
    @WithBurgerUser("123")
    fun getZakenOnlyOpen() {
        val query =
            """
            query {
                getZaken(page: 1, isOpen: true) {
                number
                size
                totalPages
                totalElements
                numberOfElements
                content {
                        uuid,
                        identificatie,
                        omschrijving,
                        zaaktype {
                            identificatie,
                            omschrijving
                        },
                        startdatum,
                        status {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        },
                        statusGeschiedenis {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        }
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getZaken"
        val resultPath = "$basePath.content[0]"

        val response =
            testClient.post()
                .uri("/graphql")
                .accept(APPLICATION_JSON)
                .contentType(MediaType("application", "graphql"))
                .bodyValue(query)
                .exchange()
                .expectBody()
                .consumeWith(Consumer { t -> logger.info { t } })

        response
            .jsonPath(basePath).exists()
            .jsonPath("$resultPath.uuid").isEqualTo("5d479908-fbb7-49c2-98c9-9afecf8de79a")
            .jsonPath("$resultPath.identificatie").isEqualTo("ZAAK-2021-0000000003")
            .jsonPath("$resultPath.omschrijving").isEqualTo("Voorbeeld afgesloten zaak 1")
            .jsonPath("$resultPath.startdatum").isEqualTo("2021-09-16")
            .jsonPath("$resultPath.zaaktype.identificatie").isEqualTo("bezwaar-behandelen")
            .jsonPath("$resultPath.zaaktype.omschrijving").isEqualTo("Bezwaar behandelen")
            .jsonPath("$resultPath.status.datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$resultPath.status.statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$resultPath.status.statustype.isEindstatus").isEqualTo(true)
            .jsonPath("$resultPath.statusGeschiedenis[0].datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$resultPath.statusGeschiedenis[0].statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$resultPath.statusGeschiedenis[0].statustype.isEindstatus").isEqualTo(true)
    }

    @Test
    @WithBurgerUser("123")
    fun getZakenFilterOfIdentificatie() {
        val query =
            """
            query {
                getZaken(page: 1, identificatie: "ZAAK-2024-0000000001") {
                number
                size
                totalPages
                totalElements
                numberOfElements
                content {
                        uuid,
                        identificatie,
                        omschrijving,
                        startdatum
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getZaken"
        val resultPath = "$basePath.content[0]"

        val response =
            testClient.post()
                .uri("/graphql")
                .accept(APPLICATION_JSON)
                .contentType(MediaType("application", "graphql"))
                .bodyValue(query)
                .exchange()
                .expectBody()
                .consumeWith(Consumer { t -> logger.info { t } })

        response
            .jsonPath(basePath).exists()
            .jsonPath("$resultPath.uuid").isEqualTo("5d479908-fbb7-49c2-98c9-9afecf8de79a")
            .jsonPath("$resultPath.identificatie").isEqualTo("ZAAK-2024-0000000001")
            .jsonPath("$resultPath.omschrijving").isEqualTo("Voorbeeld afgesloten zaak 1")
            .jsonPath("$resultPath.startdatum").isEqualTo("2021-09-16")
    }

    @Test
    @WithBurgerUser("")
    fun getZakenNotFound() {
        // Make the GraphQL request
        testClient.post()
            .uri("/not_found")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .exchange()
            .expectStatus().isNotFound() // Assert NOT_FOUND status
    }

    @Test
    fun getZakenUnAuthorized() {
        zakenApiConfig.clientId = ""

        val query =
            """
            query {
                getZaken(page: 0) {
                    number
                size
                totalPages
                totalElements
                numberOfElements
                content {
                        uuid,
                        identificatie,
                        omschrijving,
                        zaaktype {
                            identificatie,
                            omschrijving
                        },
                        startdatum,
                        status {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        },
                        statusGeschiedenis {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        }
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getZaken"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
    }

    @Test
    @WithBurgerUser("123")
    fun `getZaken no page`() {
        val query =
            """
            query {
                getZaken {
                    number
                size
                totalPages
                totalElements
                numberOfElements
                content {
                        uuid,
                        identificatie,
                        omschrijving,
                        zaaktype {
                            identificatie,
                            omschrijving
                        },
                        startdatum,
                        status {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        },
                        statusGeschiedenis {
                            datumStatusGezet,
                            statustype {
                                omschrijving,
                                isEindstatus
                            }
                        }
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getZaken"
        val resultPath = "$basePath.content[0]"
        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$resultPath.uuid").isEqualTo("5d479908-fbb7-49c2-98c9-9afecf8de79a")
            .jsonPath("$resultPath.identificatie").isEqualTo("ZAAK-2021-0000000003")
            .jsonPath("$resultPath.omschrijving").isEqualTo("Voorbeeld afgesloten zaak 1")
            .jsonPath("$resultPath.startdatum").isEqualTo("2021-09-16")
            .jsonPath("$resultPath.zaaktype.identificatie").isEqualTo("bezwaar-behandelen")
            .jsonPath("$resultPath.zaaktype.omschrijving").isEqualTo("Bezwaar behandelen")
            .jsonPath("$resultPath.status.datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$resultPath.status.statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$resultPath.status.statustype.isEindstatus").isEqualTo(true)
            .jsonPath("$resultPath.statusGeschiedenis[0].datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$resultPath.statusGeschiedenis[0].statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$resultPath.statusGeschiedenis[0].statustype.isEindstatus").isEqualTo(true)
    }

    @Test
    @WithBurgerUser("123")
    fun getZaak() {
        val query =
            """
            query {
                getZaak(id: "5d479908-fbb7-49c2-98c9-9afecf8de79a") {
                    uuid,
                    identificatie,
                    omschrijving,
                    zaaktype {
                        identificatie,
                        omschrijving
                    },
                    startdatum,
                    status {
                        datumStatusGezet,
                        statustype {
                            omschrijving,
                            isEindstatus
                        }
                    },
                    statusGeschiedenis {
                        datumStatusGezet,
                        statustype {
                            omschrijving,
                            isEindstatus
                        }
                    },
                    documenten {
                        uuid,
                        documentapi,
                        identificatie,
                        creatiedatum,
                        titel,
                        formaat,
                        bestandsnaam,
                        bestandsomvang
                    },
                    statussen {
                        omschrijving,
                        isEindstatus
                    },
                    besluiten {
                        identificatie,
                        datum,
                        toelichting,
                        publicatiedatum
                    }
                }
            }
            """.trimIndent()

        val basePath = "$.data.getZaak"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .consumeWith(System.out::println)
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.uuid").isEqualTo("5d479908-fbb7-49c2-98c9-9afecf8de79a")
            .jsonPath("$basePath.identificatie").isEqualTo("ZAAK-2021-0000000003")
            .jsonPath("$basePath.omschrijving").isEqualTo("Voorbeeld afgesloten zaak 1")
            .jsonPath("$basePath.startdatum").isEqualTo("2021-09-16")
            .jsonPath("$basePath.zaaktype.identificatie").isEqualTo("bezwaar-behandelen")
            .jsonPath("$basePath.zaaktype.omschrijving").isEqualTo("Bezwaar behandelen")
            .jsonPath("$basePath.status.datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$basePath.status.statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$basePath.status.statustype.isEindstatus").isEqualTo(true)
            .jsonPath("$basePath.statusGeschiedenis[0].datumStatusGezet").isEqualTo("2021-09-16T14:00:00Z")
            .jsonPath("$basePath.statusGeschiedenis[0].statustype.omschrijving").isEqualTo("Zaak afgerond")
            .jsonPath("$basePath.statusGeschiedenis[0].statustype.isEindstatus").isEqualTo(true)
            .jsonPath("$basePath.documenten[0].uuid").isEqualTo("095be615-a8ad-4c33-8e9c-c7612fbf6c9f")
            .jsonPath("$basePath.documenten[0].titel").isEqualTo("Een titel")
            .jsonPath("$basePath.documenten[0].formaat").isEqualTo(".pdf")
            .jsonPath("$basePath.statussen[0].omschrijving").isEqualTo("Eerste status")
            .jsonPath("$basePath.statussen[0].isEindstatus").isEqualTo(false)
            .jsonPath("$basePath.statussen[2].omschrijving").isEqualTo("Derde status")
            .jsonPath("$basePath.statussen[2].isEindstatus").isEqualTo(true)
            .jsonPath("$basePath.besluiten[0].identificatie").isEqualTo("klantportaal")
    }

    fun setupMockOpenZaakServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                    val response =
                        when (path) {
                            "/zaken/api/v1/zaken" -> {
                                if (queryParams.any { it.contains("ZAAK-2024-0000000001") }) {
                                    handleZaakListRequestOfIdentificatie()
                                } else {
                                    handleZaakListRequest()
                                }
                            }
                            "/zaken/api/v1/statussen/0c019c8a-2274-4a7b-b381-2f35908500a6" -> handleStatusRequest()
                            "/zaken/api/v1/statussen" -> handleStatusListRequest()
                            "/catalogi/api/v1/zaaktypen/496f51fd-ccdb-406e-805a-e7602ae78a2b" -> handleZaakTypeRequest()
                            "/catalogi/api/v1/statustypen" -> handleStatusTypenRequest()
                            "/catalogi/api/v1/statustypen/a4bd90f4-b80c-446b-9f68-62c5b39298ff" -> handleStatusTypeRequest()
                            "/zaken/api/v1/zaken/5d479908-fbb7-49c2-98c9-9afecf8de79a" -> handleZaakRequest()
                            "/zaken/api/v1/zaakinformatieobjecten" -> handleZaakInformatieObjectenRequest()
                            "/enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f" -> handleDocumentRequest()
                            "/zaken/api/v1/rollen" -> handleZaakRollenRequest()
                            "/besluiten/api/v1/besluiten" -> handleBesluitenRequest()
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }

    fun handleZaakListRequest(): MockResponse {
        val body =
            """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "$url/zaken/api/v1/zaken/5d479908-fbb7-49c2-98c9-9afecf8de79a",
                        "uuid": "5d479908-fbb7-49c2-98c9-9afecf8de79a",
                        "identificatie": "ZAAK-2021-0000000003",
                        "bronorganisatie": "051845623",
                        "omschrijving": "Voorbeeld afgesloten zaak 1",
                        "toelichting": "",
                        "zaaktype": "http://localhost:8000/catalogi/api/v1/zaaktypen/496f51fd-ccdb-406e-805a-e7602ae78a2b",
                        "registratiedatum": "2021-09-16",
                        "verantwoordelijkeOrganisatie": "051845623",
                        "startdatum": "2021-09-16",
                        "einddatum": null,
                        "einddatumGepland": null,
                        "uiterlijkeEinddatumAfdoening": null,
                        "publicatiedatum": null,
                        "communicatiekanaal": "",
                        "productenOfDiensten": [],
                        "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                        "betalingsindicatie": "",
                        "betalingsindicatieWeergave": "",
                        "laatsteBetaaldatum": null,
                        "zaakgeometrie": null,
                        "verlenging": {
                            "reden": "",
                            "duur": null
                        },
                        "opschorting": {
                            "indicatie": false,
                            "reden": ""
                        },
                        "selectielijstklasse": "",
                        "hoofdzaak": null,
                        "deelzaken": [],
                        "relevanteAndereZaken": [],
                        "eigenschappen": [],
                        "status": "http://localhost:8000/zaken/api/v1/statussen/0c019c8a-2274-4a7b-b381-2f35908500a6",
                        "kenmerken": [],
                        "archiefnominatie": null,
                        "archiefstatus": "nog_te_archiveren",
                        "archiefactiedatum": null,
                        "resultaat": null
                    }
                ]
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakListRequestOfIdentificatie(): MockResponse {
        val body =
            """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "$url/zaken/api/v1/zaken/5d479908-fbb7-49c2-98c9-9afecf8de79a",
                        "uuid": "5d479908-fbb7-49c2-98c9-9afecf8de79a",
                        "identificatie": "ZAAK-2024-0000000001",
                        "bronorganisatie": "051845623",
                        "omschrijving": "Voorbeeld afgesloten zaak 1",
                        "toelichting": "",
                        "zaaktype": "http://localhost:8000/catalogi/api/v1/zaaktypen/496f51fd-ccdb-406e-805a-e7602ae78a2b",
                        "registratiedatum": "2021-09-16",
                        "verantwoordelijkeOrganisatie": "051845623",
                        "startdatum": "2021-09-16",
                        "einddatum": null,
                        "einddatumGepland": null,
                        "uiterlijkeEinddatumAfdoening": null,
                        "publicatiedatum": null,
                        "communicatiekanaal": "",
                        "productenOfDiensten": [],
                        "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                        "betalingsindicatie": "",
                        "betalingsindicatieWeergave": "",
                        "laatsteBetaaldatum": null,
                        "zaakgeometrie": null,
                        "verlenging": {
                            "reden": "",
                            "duur": null
                        },
                        "opschorting": {
                            "indicatie": false,
                            "reden": ""
                        },
                        "selectielijstklasse": "",
                        "hoofdzaak": null,
                        "deelzaken": [],
                        "relevanteAndereZaken": [],
                        "eigenschappen": [],
                        "status": "http://localhost:8000/zaken/api/v1/statussen/0c019c8a-2274-4a7b-b381-2f35908500a6",
                        "kenmerken": [],
                        "archiefnominatie": null,
                        "archiefstatus": "nog_te_archiveren",
                        "archiefactiedatum": null,
                        "resultaat": null
                    }
                ]
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakRequest(): MockResponse {
        val body =
            """
            {
                "url": "$url/zaken/api/v1/zaken/5d479908-fbb7-49c2-98c9-9afecf8de79a",
                "uuid": "5d479908-fbb7-49c2-98c9-9afecf8de79a",
                "identificatie": "ZAAK-2021-0000000003",
                "bronorganisatie": "051845623",
                "omschrijving": "Voorbeeld afgesloten zaak 1",
                "toelichting": "",
                "zaaktype": "http://localhost:8000/catalogi/api/v1/zaaktypen/496f51fd-ccdb-406e-805a-e7602ae78a2b",
                "registratiedatum": "2021-09-16",
                "verantwoordelijkeOrganisatie": "051845623",
                "startdatum": "2021-09-16",
                "einddatum": null,
                "einddatumGepland": null,
                "uiterlijkeEinddatumAfdoening": null,
                "publicatiedatum": null,
                "communicatiekanaal": "",
                "productenOfDiensten": [],
                "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                "betalingsindicatie": "",
                "betalingsindicatieWeergave": "",
                "laatsteBetaaldatum": null,
                "zaakgeometrie": null,
                "verlenging": {
                    "reden": "",
                    "duur": null
                },
                "opschorting": {
                    "indicatie": false,
                    "reden": ""
                },
                "selectielijstklasse": "",
                "hoofdzaak": null,
                "deelzaken": [],
                "relevanteAndereZaken": [],
                "eigenschappen": [],
                "status": "http://localhost:8000/zaken/api/v1/statussen/0c019c8a-2274-4a7b-b381-2f35908500a6",
                "kenmerken": [],
                "archiefnominatie": null,
                "archiefstatus": "nog_te_archiveren",
                "archiefactiedatum": null,
                "resultaat": null
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleStatusListRequest(): MockResponse {
        val body =
            """
            {
                "count": 1,
                "next": null,
                "previous": null,
                "results": [
                    {
                        "url": "http://localhost:8000/zaken/api/v1/statussen/7fd765f5-ce02-475c-8091-0203c531e41f",
                        "uuid": "7fd765f5-ce02-475c-8091-0203c531e41f",
                        "zaak": "http://localhost:8000/zaken/api/v1/zaken/e163caad-1ca4-4ad4-9ac3-6aeb6b8122ce",
                        "statustype": "http://localhost:8000/catalogi/api/v1/statustypen/a4bd90f4-b80c-446b-9f68-62c5b39298ff",
                        "datumStatusGezet": "2021-09-16T14:00:00Z",
                        "statustoelichting": ""
                    }
                ]
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleStatusRequest(): MockResponse {
        val body =
            """
            {
                "url": "http://localhost:8000/zaken/api/v1/statussen/7fd765f5-ce02-475c-8091-0203c531e41f",
                "uuid": "7fd765f5-ce02-475c-8091-0203c531e41f",
                "zaak": "http://localhost:8000/zaken/api/v1/zaken/e163caad-1ca4-4ad4-9ac3-6aeb6b8122ce",
                "statustype": "http://localhost:8000/catalogi/api/v1/statustypen/a4bd90f4-b80c-446b-9f68-62c5b39298ff",
                "datumStatusGezet": "2021-09-16T14:00:00Z",
                "statustoelichting": ""
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleStatusTypeRequest(): MockResponse {
        val body =
            """
            {
                "url": "http://localhost:8000/catalogi/api/v1/statustypen/3c8f06ab-1c69-4154-9850-31bdb649e376",
                "omschrijving": "Zaak afgerond",
                "omschrijvingGeneriek": "Zaak afgerond",
                "statustekst": "test",
                "zaaktype": "http://localhost:8000/catalogi/api/v1/zaaktypen/496f51fd-ccdb-406e-805a-e7602ae78a2b",
                "volgnummer": 7,
                "isEindstatus": true,
                "informeren": true
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakTypeRequest(): MockResponse {
        val body =
            """
            {
                "url": "http://localhost:8000/catalogi/api/v1/zaaktypen/496f51fd-ccdb-406e-805a-e7602ae78a2b",
                "identificatie": "bezwaar-behandelen",
                "omschrijving": "Bezwaar behandelen",
                "omschrijvingGeneriek": "Bezwaar behandelen",
                "vertrouwelijkheidaanduiding": "zaakvertrouwelijk",
                "doel": "Een uitspraak doen op een ingekomen bezwaar tegen een eerder genomen besluit.",
                "aanleiding": "Er is een bezwaarschrift ontvangen tegen een besluit dat genomen is door de gemeente.",
                "toelichting": "Conform de Algemene Wet Bestuursrecht (AWB) heeft een natuurlijk of niet-natuurlijk persoon de mogelijkheid om bezwaar te maken tegen een genomen besluit van de gemeente, bijvoorbeeld het niet verlenen van een vergunning.",
                "indicatieInternOfExtern": "extern",
                "handelingInitiator": "Indienen",
                "onderwerp": "Bezwaar",
                "handelingBehandelaar": "Behandelen",
                "doorlooptijd": "P84D",
                "servicenorm": null,
                "opschortingEnAanhoudingMogelijk": false,
                "verlengingMogelijk": true,
                "verlengingstermijn": "P42D",
                "trefwoorden": [
                    "bezwaar",
                    "bezwaarschrift"
                ],
                "publicatieIndicatie": false,
                "publicatietekst": "",
                "verantwoordingsrelatie": [],
                "productenOfDiensten": [
                    "https://github.com/valtimo-platform/valtimo-platform"
                ],
                "selectielijstProcestype": "https://selectielijst.openzaak.nl/api/v1/procestypen/e1b73b12-b2f6-4c4e-8929-94f84dd2a57d",
                "referentieproces": {
                    "naam": "Bezwaar behandelen",
                    "link": "http://www.gemmaonline.nl/index.php/Referentieproces_bezwaar_behandelen"
                },
                "catalogus": "http://localhost:8000/catalogi/api/v1/catalogussen/8225508a-6840-413e-acc9-6422af120db1",
                "statustypen": [
                    "http://localhost:8000/catalogi/api/v1/statustypen/3c8f06ab-1c69-4154-9850-31bdb649e376",
                    "http://localhost:8000/catalogi/api/v1/statustypen/578cd763-86f0-41fe-98ea-ac0edd4fa20a",
                    "http://localhost:8000/catalogi/api/v1/statustypen/21bd8aab-9c58-4bde-9968-7fb0ab12378e",
                    "http://localhost:8000/catalogi/api/v1/statustypen/996931e5-290f-45ca-8b72-c0e205491b19",
                    "http://localhost:8000/catalogi/api/v1/statustypen/a4bd90f4-b80c-446b-9f68-62c5b39298ff",
                    "http://localhost:8000/catalogi/api/v1/statustypen/70a9b5b8-6d06-47b1-bafc-d73934c4454e",
                    "http://localhost:8000/catalogi/api/v1/statustypen/404b3e60-b70e-4366-a6e6-6ecc3a1a1c49"
                ],
                "resultaattypen": [],
                "eigenschappen": [],
                "informatieobjecttypen": [],
                "roltypen": [
                    "http://localhost:8000/catalogi/api/v1/roltypen/1c359a1b-c38d-47b8-bed5-994db88ead61"
                ],
                "besluittypen": [],
                "deelzaaktypen": [],
                "gerelateerdeZaaktypen": [],
                "beginGeldigheid": "2021-01-01",
                "eindeGeldigheid": null,
                "versiedatum": "2021-01-01",
                "concept": false
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakInformatieObjectenRequest(): MockResponse {
        val body =
            """
            [
               {
                 "url": "$url",
                 "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                 "informatieobject": "$url/095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
                 "zaak": "$url",
                 "aardRelatieWeergave": "Hoort bij, omgekeerd: kent",
                 "titel": "string",
                 "beschrijving": "string",
                 "registratiedatum": "2019-08-24T14:15:22Z"
               }
            ]
            """.trimIndent()
        return mockResponse(body)
    }

    fun handleDocumentRequest(): MockResponse {
        val body =
            """
            {
               "url": "http://some.domain.com/enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
               "identificatie": "string",
               "bronorganisatie": "string",
               "creatiedatum": "2021-10-14",
               "titel": "Een titel",
               "vertrouwelijkheidaanduiding": "openbaar",
               "auteur": "string",
               "status": "definitief",
               "formaat": ".pdf",
               "taal": "str",
               "versie": 0,
               "beginRegistratie": "2021-10-14T12:27:43Z",
               "bestandsnaam": "string",
               "inhoud": "http://example.com",
               "bestandsomvang": 0,
               "link": "http://example.com",
               "beschrijving": "string",
               "ontvangstdatum": "2021-10-14",
               "verzenddatum": "2021-10-14",
               "indicatieGebruiksrecht": true,
               "ondertekening": {
                 "soort": "analoog",
                 "datum": "2021-10-14"
               },
               "integriteit": {
                 "algoritme": "crc_16",
                 "waarde": "string",
                 "datum": "2021-10-14"
               },
               "informatieobjecttype": "http://example.com",
               "locked": true,
               "bestandsdelen": [
                 {
                   "url": "http://example.com",
                   "volgnummer": 0,
                   "omvang": 0,
                   "inhoud": "http://example.com",
                   "voltooid": true,
                   "lock": "string"
                 }
               ]
             }
            """.trimIndent()
        return mockResponse(body)
    }

    fun handleStatusTypenRequest(): MockResponse {
        val body =
            """
            {
               "count": 3,
               "next": "http://example.com",
               "previous": "http://example.com",
               "results": [
                 {
                   "url": "http://example.com",
                   "omschrijving": "Derde status",
                   "omschrijvingGeneriek": "string",
                   "statustekst": "string",
                   "zaaktype": "http://example.com",
                   "volgnummer": 3,
                   "isEindstatus": true,
                   "informeren": true
                 },
                 {
                   "url": "http://example.com",
                   "omschrijving": "Eerste status",
                   "omschrijvingGeneriek": "string",
                   "statustekst": "string",
                   "zaaktype": "http://example.com",
                   "volgnummer": 1,
                   "isEindstatus": false,
                   "informeren": true
                 },
                 {
                   "url": "http://example.com",
                   "omschrijving": "Tweede status",
                   "omschrijvingGeneriek": "string",
                   "statustekst": "string",
                   "zaaktype": "http://example.com",
                   "volgnummer": 2,
                   "isEindstatus": false,
                   "informeren": true
                 }
               ]
             }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleZaakRollenRequest(): MockResponse {
        val body =
            """
            {
               "count": 3,
               "next": null,
               "previous": null,
               "results": [
                 {
                   "url": "http://example.com",
                   "uuid": "095be615-a8ad-4c33-8e9c-c7612fbf6c9f",
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
               ]
             }
            """.trimIndent()

        return mockResponse(body)
    }

    fun handleBesluitenRequest(): MockResponse {
        val body =
            """
            {
              "count": 2,
              "next": "http://localhost:8001/besluiten/api/v1/besluiten?page=2",
              "previous": null,
              "results": [
                {
                  "url": "http://localhost:8001/besluiten/api/v1/besluiten/496f51fd-ccdb-406e-805a-e7602ae78a2z",
                  "identificatie": "klantportaal",
                  "verantwoordelijkeOrganisatie": "klantportaal",
                  "besluittype": "http://localhost:8000/catalogi/api/v1/besluittypen/496f51fd-ccdb-406e-805a-e7602ae78a2b",
                  "zaak": "http://localhost:8001/zaken/api/v1/zaken/496f51fd-ccdb-406e-805a-e7602ae78a2x",
                  "datum": "2019-08-24",
                  "toelichting": "toelichting",
                  "bestuursorgaan": "klant",
                  "ingangsdatum": "2019-08-24",
                  "vervaldatum": "2019-08-24",
                  "vervalreden": "tijdelijk",
                  "vervalredenWeergave": "string",
                  "publicatiedatum": "2019-08-24",
                  "verzenddatum": "2019-08-24",
                  "uiterlijkeReactiedatum": "2019-08-24"
                }
              ]
            }
            """.trimIndent()

        return mockResponse(body)
    }

    fun mockResponse(body: String): MockResponse {
        return MockResponse().addHeader("Content-Type", "application/json; charset=utf-8").setResponseCode(200)
            .setBody(body)
    }
}