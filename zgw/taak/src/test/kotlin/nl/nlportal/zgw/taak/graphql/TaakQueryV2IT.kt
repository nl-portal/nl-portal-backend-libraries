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
package nl.nlportal.zgw.taak.graphql

import nl.nlportal.commonground.authentication.WithBedrijfUser
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import nl.nlportal.zgw.taak.TestHelper
import nl.nlportal.zgw.taak.TestHelper.verifyOnlyDataExists
import nl.nlportal.zgw.taak.domain.TaakSoort
import nl.nlportal.zgw.taak.domain.TaakStatus
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class TaakQueryV2IT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
) {
    lateinit var server: MockWebServer

    @Autowired
    private lateinit var getTakenPayloadV2: String

    @Autowired
    private lateinit var getTaakByIdPayloadV2: String

    @Autowired
    private lateinit var getTaakByIdPayloadV2Bedrijf: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockObjectsApiServer()
        server.start()
        objectsApiClientConfig.url = server.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    // Disabled durin migratiom from V1 to V2
    @Test
    @WithBurgerUser("569312863")
    fun `should get list of tasks for burger`() {
        val basePath = "$.data.getTakenV2"
        val resultPath = "$basePath.content[0]"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTakenPayloadV2)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$resultPath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce708")
            .jsonPath("$resultPath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$resultPath.soort").isEqualTo(TaakSoort.PORTAALFORMULIER.name)
            .jsonPath("$resultPath.verloopdatum").isEqualTo("2023-09-20T18:25:43.524")
            .jsonPath(
                "$resultPath.portaalformulier.formulier.value",
            ).isEqualTo("http://localhost:8010/api/v2/objects/4e40fb4c-a29a-4e48-944b-c34a1ff6c8f4")
            .jsonPath("$basePath.number").isEqualTo(1)
            .jsonPath("$basePath.size").isEqualTo(1)
            .jsonPath("$basePath.totalPages").isEqualTo(2)
            .jsonPath("$basePath.totalElements").isEqualTo(2)
            .jsonPath("$basePath.numberOfElements").isEqualTo(2)
    }

    // Disabled durin migratiom from V1 to V2
    // @Test
    @WithBedrijfUser("14127293")
    fun `should get list of tasks for bedrijf`() {
        val basePath = "$.data.getTakenV2"
        val resultPath = "$basePath.content[0]"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTakenPayloadV2)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$resultPath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce708")
            .jsonPath("$resultPath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$resultPath.verloopdatum").isEqualTo("2023-09-20T18:25:43.524")
            .jsonPath(
                "$resultPath.portaalformulier.formulier",
            ).isEqualTo("http://localhost:8010/api/v2/objects/4e40fb4c-a29a-4e48-944b-c34a1ff6c8f4")
            .jsonPath("$resultPath.portaalformulier.data.voornaam").isEqualTo("Jan")
            .jsonPath("$basePath.number").isEqualTo(1)
            .jsonPath("$basePath.size").isEqualTo(1)
            .jsonPath("$basePath.totalPages").isEqualTo(2)
            .jsonPath("$basePath.totalElements").isEqualTo(2)
            .jsonPath("$basePath.numberOfElements").isEqualTo(1)
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should get task by id for burger`() {
        val basePath = "$.data.getTaakByIdV2"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTaakByIdPayloadV2)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath(
                "$basePath.portaalformulier.formulier.value",
            ).isEqualTo("http://localhost:8010/api/v2/objects/4e40fb4c-a29a-4e48-944b-c34a1ff6c8f4")
        // .jsonPath("$basePath.portaalformulier.data.voornaam").isEqualTo("Jan")
        // .jsonPath("$basePath.status").isEqualTo(TaakStatus.OPEN.toString())
        // .jsonPath("$basePath.verloopdatum").isEqualTo("2023-09-20T18:25:43.524")
    }

    @Test
    @WithBedrijfUser("14127293")
    fun `should get task by id for bedrijf`() {
        val basePath = "$.data.getTaakByIdV2"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTaakByIdPayloadV2Bedrijf)
            .exchange()
            .verifyOnlyDataExists(basePath)
            .jsonPath("$basePath.id").isEqualTo("2d725c07-2f26-4705-8637-438a42b5ac2d")
            .jsonPath(
                "$basePath.portaalformulier.formulier.value",
            ).isEqualTo("http://localhost:8010/api/v2/objects/4e40fb4c-a29a-4e48-944b-c34a1ff6c8f4")
            .jsonPath("$basePath.portaalformulier.data.voornaam").isEqualTo("Jan")
            .jsonPath("$basePath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$basePath.verloopdatum").isEqualTo("2023-09-20T18:25:43.524")
    }

    @Test
    @WithBurgerUser("569312864")
    fun `should unauthorized get task by id for burger`() {
        val basePath = "$.data.getTaakByIdV2"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTaakByIdPayloadV2)
            .exchange()
            .expectBody()
            .jsonPath(basePath)
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
                            "GET /api/v2/objects" -> {
                                if (queryParams.any { it.contains("identificatie__value__exact__569312863") } &&
                                    queryParams.any {
                                        it.contains(
                                            "type=http://localhost:8011/api/v1/objecttypes/3c24cab6-4346-4c7d-912b-e34a1e9e21bf",
                                        )
                                    }
                                ) {
                                    TestHelper.mockResponseFromFile("/data/get-bsn-task-list-v2.json")
                                } else if (queryParams.any { it.contains("identificatie__value__exact__569312863") }) {
                                    TestHelper.mockResponseFromFile("/data/get-bsn-task-list.json")
                                } else if (queryParams.any { it.contains("identificatie__value__exact__569312863") }) {
                                    TestHelper.mockResponseFromFile("/data/get-bsn-task-list-unauthorized-v2.json")
                                } else if (queryParams.any { it.contains("identificatie__value__exact__14127293") }) {
                                    TestHelper.mockResponseFromFile("/data/get-kvk-task-list-v2.json")
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }
                            "GET /api/v2/objects/58fad5ab-dc2f-11ec-9075-f22a405ce708" -> {
                                TestHelper.mockResponseFromFile("/data/get-taskv2.json")
                            }
                            "GET /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5ac2d" -> {
                                TestHelper.mockResponseFromFile("/data/get-taskv2-bedrijf.json")
                            }
                            //
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }
}