/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.gzac.objectsapi.task.graphql

import com.ritense.portal.commonground.authentication.WithBurgerUser
import com.ritense.portal.gzac.objectsapi.TestHelper
import com.ritense.portal.gzac.objectsapi.autoconfiguration.ObjectsApiClientConfig
import com.ritense.portal.gzac.objectsapi.task.domain.TaskStatus
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
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class TaskMutationIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig
) {
    lateinit var server: MockWebServer

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
    fun `should submit task`() {

        val query = """
            {
                "query": "
                    mutation (${"$"}id: UUID!, ${"$"}submission: JSON!) {
                        submitTask(id: ${"$"}id, submission: ${"$"}submission) {
                            id
                            objectId
                            formId
                            status
                            date
                        }
                    }",
                "variables": {
                    "id": "58fad5ab-dc2f-11ec-9075-f22a405ce707",
                    "submission": {
                        "firstName": "John"
                    }
                }
            }
        """.trimIndent().replace('\n', ' ')

        val basePath = "$.data.submitTask"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.id").isEqualTo("58fad5ab-dc2f-11ec-9075-f22a405ce707")
            .jsonPath("$basePath.status").isEqualTo(TaskStatus.INGEDIEND.toString())
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
                        } else {
                            MockResponse().setResponseCode(404)
                        }
                    }

                    "PUT /api/v2/objects/2d725c07-2f26-4705-8637-438a42b5ac2d" ->
                        TestHelper.mockResponseFromFile("/data/put-objectsapi-task-response.json")

                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }
}