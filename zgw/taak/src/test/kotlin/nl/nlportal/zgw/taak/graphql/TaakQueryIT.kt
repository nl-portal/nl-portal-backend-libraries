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

import com.ritense.portal.commonground.authentication.WithBedrijfUser
import com.ritense.portal.commonground.authentication.WithBurgerUser
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import nl.nlportal.zgw.taak.TestHelper
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
internal class TaakQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig
) {
    lateinit var server: MockWebServer

    @Autowired
    private lateinit var getTakenPayload: String

    @Autowired
    private lateinit var getTaakByIdPayload: String

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockObjectsApiServer()
        server.start()
        objectsApiClientConfig.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should get list of tasks for burger`() {
        val basePath = "$.data.getTaken"
        val resultPath = "$basePath.content[0]"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTakenPayload)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$resultPath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath("$resultPath.objectId").isEqualTo("2d725c07-2f26-4705-8637-438a42b5ac2d")
            .jsonPath("$resultPath.formId").isEqualTo("check-loan-form")
            .jsonPath("$resultPath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$resultPath.date").isEqualTo("2022-05-25")
            .jsonPath("$resultPath.data.voornaam").isEqualTo("Peter")
            .jsonPath("$basePath.number").isEqualTo(1)
            .jsonPath("$basePath.size").isEqualTo(1)
            .jsonPath("$basePath.totalPages").isEqualTo(2)
            .jsonPath("$basePath.totalElements").isEqualTo(2)
            .jsonPath("$basePath.numberOfElements").isEqualTo(1)
    }

    @Test
    @WithBedrijfUser("14127293")
    fun `should get list of tasks for bedrijf`() {
        val basePath = "$.data.getTaken"
        val resultPath = "$basePath.content[0]"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTakenPayload)
            .exchange()
            .expectBody()
            .jsonPath(resultPath).exists()
            .jsonPath("$resultPath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath("$resultPath.objectId").isEqualTo("2d94fedb-3d99-43c4-b333-f04e0ccfe78a")
            .jsonPath("$resultPath.formId").isEqualTo("check-loan-form")
            .jsonPath("$resultPath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$resultPath.date").isEqualTo("2022-05-30")
            .jsonPath("$resultPath.data.voornaam").isEqualTo("Peter")
            .jsonPath("$basePath.number").isEqualTo(1)
            .jsonPath("$basePath.size").isEqualTo(1)
            .jsonPath("$basePath.totalPages").isEqualTo(2)
            .jsonPath("$basePath.totalElements").isEqualTo(2)
            .jsonPath("$basePath.numberOfElements").isEqualTo(1)
    }

    @Test
    @WithBedrijfUser("14127293")
    fun `should get task by id for burger`() {
        val basePath = "$.data.getTaakById"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTaakByIdPayload)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath("$basePath.formId").isEqualTo("check-loan-form")
            .jsonPath("$basePath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$basePath.date").isEqualTo("2022-05-30")
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should get task by id for bedrijf`() {
        val basePath = "$.data.getTaakById"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(getTaakByIdPayload)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath("$basePath.formId").isEqualTo("check-loan-form")
            .jsonPath("$basePath.status").isEqualTo(TaakStatus.OPEN.toString())
            .jsonPath("$basePath.date").isEqualTo("2022-05-25")
    }

    fun setupMockObjectsApiServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path?.substringBefore('?')
                val queryParams = request.path?.substringAfter('?')?.split('&') ?: emptyList()
                val response = when (request.method + " " + path) {
                    "GET /api/v2/objects" -> {
                        if (queryParams.any { it.contains("bsn__exact__569312863") }) {
                            TestHelper.mockResponseFromFile("/data/get-bsn-task-list.json")
                        } else if (queryParams.any { it.contains("kvk__exact__14127293") }) {
                            TestHelper.mockResponseFromFile("/data/get-kvk-task-list.json")
                        } else {
                            MockResponse().setResponseCode(404)
                        }
                    }

                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }
}