/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.graphql

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.berichten.TestHelper
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BerichtenQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val documentApisConfig: DocumentApisConfig,
) {
    lateinit var mockObjectenApi: MockWebServer

    @BeforeEach
    fun setUp() {
        mockObjectenApi = MockWebServer()
        setupMockObjectsApiServer()
        mockObjectenApi.start(port = 10001)
        objectsApiClientConfig.properties.url = mockObjectenApi.url("/").toUri()
        documentApisConfig.properties.getConfig("openzaak").url = mockObjectenApi.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        mockObjectenApi.shutdown()
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return BerichtenPage`() {
        val responseBody =
            httpGraphQlTester
                .document(TestHelper.graphqlBerichtenPageRequest)
                .execute()
                .errors()
                .verify()
                .path("getBerichten")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("NOTIFICATIE", responseBody.requiredAt("/content/0/berichtType")?.textValue())
        assertEquals(false, responseBody.requiredAt("/content/0/geopend")?.booleanValue())
        assertEquals("2024-07-18T18:25:43.524", responseBody.requiredAt("/content/0/publicatiedatum")?.textValue())
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should update bericht before return`() {
        val responseBody =
            httpGraphQlTester
                .document(TestHelper.graphqlValidBerichtRequest)
                .execute()
                .errors()
                .verify()
                .path("getBericht")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("NOTIFICATIE", responseBody.get("berichtType").textValue())
        assertEquals(true, responseBody.get("geopend").booleanValue())
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should return bericht`() {
        val responseBody =
            httpGraphQlTester
                .document(TestHelper.graphqlValidBerichtReadRequest)
                .execute()
                .errors()
                .verify()
                .path("getBericht")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("NOTIFICATIE", responseBody.get("berichtType").textValue())
        assertEquals(true, responseBody.get("geopend").booleanValue())
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should return bericht met documenten`() {
        val responseBody =
            httpGraphQlTester
                .document(TestHelper.graphqlValidBerichtReadRequestWithDocuments)
                .execute()
                .errors()
                .verify()
                .path("getBericht")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("NOTIFICATIE", responseBody.get("berichtType").textValue())
        assertEquals(true, responseBody.get("geopend").booleanValue())
    }

    @WithBurgerUser("999990755")
    @Test
    fun `should return unopened berichten`() {
        val responseBody =
            httpGraphQlTester
                .document(TestHelper.graphqlUnopenedBerichtenCountRequest)
                .execute()
                .errors()
                .verify()
                .path("getUnopenedBerichtenCount")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(11, responseBody.intValue())
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
                                if (queryParams.any { it.contains("identificatie__value__exact__999990755") }) {
                                    TestHelper.mockResponse(TestHelper.objectenApiBerichtenPageResponse)
                                } else {
                                    MockResponse().setResponseCode(404)
                                }
                            }

                            "GET /api/v2/objects/9e021130-8cbd-4c6f-846a-677448e21ce8" -> {
                                TestHelper.mockResponse(TestHelper.objectenApiBerichtObjectResponse)
                            }

                            "GET /api/v2/objects/a4961c4a-29a7-4cc7-9d5d-bceed1dfccba" -> {
                                TestHelper.mockResponse(TestHelper.objectenApiBerichtIsReadObjectResponse)
                            }

                            "PUT /api/v2/objects/9e021130-8cbd-4c6f-846a-677448e21ce8" -> {
                                TestHelper.mockResponse(TestHelper.objectenApiBerichtIsReadObjectResponse)
                            }

                            "GET /enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f" -> {
                                TestHelper.mockResponse(TestHelper.handleDocumentResponse)
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    return response
                }
            }
        mockObjectenApi.dispatcher = dispatcher
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        private val objectMapper = Mapper.get()
    }
}