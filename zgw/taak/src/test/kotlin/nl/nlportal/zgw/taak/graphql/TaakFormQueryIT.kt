/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

import com.fasterxml.jackson.databind.JsonNode
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import nl.nlportal.zgw.taak.TestHelper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
@Tag("integration")
internal class TaakFormQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockObjectsApiServer()
        server.start()
        objectsApiClientConfig.properties.url = server.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("569312863")
    fun `returns form definition for task with formulier soort=url`() {
        val formDefinition = executeForTask("58fad5ab-dc2f-11ec-9075-f22a405ce707")

        assertEquals("form", formDefinition.requiredAt("/formDefinition/display").textValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun `returns form definition for task with formulier soort=id`() {
        val formDefinition = executeForTask("44444444-4444-4444-4444-444444444444")

        assertEquals("form", formDefinition.requiredAt("/formDefinition/display").textValue())
    }

    @Test
    @WithBurgerUser("569312863")
    fun `returns null for task without portaalformulier`() {
        httpGraphQlTester
            .document(query("33333333-3333-3333-3333-333333333333"))
            .execute()
            .errors()
            .verify()
            .path("getFormDefinitionByTaskId")
            .valueIsNull()
    }

    @Test
    @WithBurgerUser("000000000")
    fun `rejects task the user does not own`() {
        httpGraphQlTester
            .document(query("58fad5ab-dc2f-11ec-9075-f22a405ce707"))
            .execute()
            .errors()
            .satisfy { errors -> assertFalse(errors.isEmpty()) }
    }

    @Test
    @WithBurgerUser("569312863")
    fun `returns error when fetched object type mismatches configured form definition type`() {
        httpGraphQlTester
            .document(query("11111111-1111-1111-1111-111111111111"))
            .execute()
            .errors()
            .satisfy { errors -> assertFalse(errors.isEmpty()) }
    }

    private fun executeForTask(taskId: String): JsonNode =
        httpGraphQlTester
            .document(query(taskId))
            .execute()
            .errors()
            .verify()
            .path("getFormDefinitionByTaskId")
            .entity(JsonNode::class.java)
            .get()

    private fun query(taskId: String): String =
        """
        query {
            getFormDefinitionByTaskId(taskId: "$taskId") {
                formDefinition
            }
        }
        """.trimIndent()

    private fun setupMockObjectsApiServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    return when (request.method + " " + path) {
                        "GET /api/v2/objects/58fad5ab-dc2f-11ec-9075-f22a405ce707" ->
                            TestHelper.mockResponseFromFile("/data/get-task-form-by-url.json")
                        "GET /api/v2/objects/44444444-4444-4444-4444-444444444444" ->
                            TestHelper.mockResponseFromFile("/data/get-task-form-by-id.json")
                        "GET /api/v2/objects/33333333-3333-3333-3333-333333333333" ->
                            TestHelper.mockResponseFromFile("/data/get-task-no-form.json")
                        "GET /api/v2/objects/11111111-1111-1111-1111-111111111111" ->
                            TestHelper.mockResponseFromFile("/data/get-task-form-mismatch.json")
                        "GET /api/v2/objects/4e40fb4c-a29a-4e48-944b-c34a1ff6c8f4" ->
                            TestHelper.mockResponseFromFile("/data/get-form-definition.json")
                        "GET /api/v2/objects/22222222-2222-2222-2222-222222222222" ->
                            TestHelper.mockResponseFromFile("/data/get-form-definition-wrong-type.json")
                        else -> MockResponse().setResponseCode(404)
                    }
                }
            }
        server.dispatcher = dispatcher
    }
}
