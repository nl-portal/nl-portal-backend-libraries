/*
 * Copyright (c) 2026 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.web.rest

import nl.nlportal.berichten.TestHelper
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BerichtDocumentResourceIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
    @Autowired private val documentApisConfig: DocumentApisConfig,
) {
    lateinit var mockServer: MockWebServer

    @BeforeEach
    fun setUp() {
        mockServer = MockWebServer()
        setupDispatcher()
        mockServer.start(port = 10001)
        objectsApiClientConfig.properties.url = mockServer.url("/").toUri()
        documentApisConfig.properties.getConfig("openzaak").url = mockServer.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    @WithBurgerUser("000000000")
    fun `should return 401 when user is not bericht recipient`() {
        webTestClient
            .get()
            .uri("/api/berichten/$OPENED_BERICHT_ID/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return 403 when bericht is not opened`() {
        webTestClient
            .get()
            .uri("/api/berichten/$UNOPENED_BERICHT_ID/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return 401 when document is not a bijlage`() {
        webTestClient
            .get()
            .uri("/api/berichten/$OPENED_BERICHT_ID/document/$UNRELATED_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return 404 when bericht not found`() {
        webTestClient
            .get()
            .uri("/api/berichten/$UNKNOWN_BERICHT_ID/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should stream content with safe Content-Disposition header for valid request`() {
        webTestClient
            .get()
            .uri("/api/berichten/$OPENED_BERICHT_ID/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Content-Disposition")
            .expectHeader()
            .valueMatches("Content-Disposition", "attachment; filename=\".*\"")
    }

    private fun setupDispatcher() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    return when (request.method + " " + path) {
                        "GET /api/v2/objects/$OPENED_BERICHT_ID" -> {
                            TestHelper.mockResponse(TestHelper.objectenApiBerichtIsReadObjectResponse)
                        }

                        "GET /api/v2/objects/$UNOPENED_BERICHT_ID" -> {
                            TestHelper.mockResponse(TestHelper.objectenApiBerichtObjectResponse)
                        }

                        "GET /enkelvoudiginformatieobjecten/$KNOWN_DOC_ID" -> {
                            TestHelper.mockResponse(TestHelper.handleDocumentResponse)
                        }

                        "GET /enkelvoudiginformatieobjecten/$KNOWN_DOC_ID/download" -> {
                            MockResponse()
                                .setResponseCode(200)
                                .setHeader("Content-Type", "application/octet-stream")
                                .setBody("file-bytes")
                        }

                        else -> {
                            MockResponse().setResponseCode(404)
                        }
                    }
                }
            }
        mockServer.dispatcher = dispatcher
    }

    companion object {
        private const val OPENED_BERICHT_ID = "a4961c4a-29a7-4cc7-9d5d-bceed1dfccba"
        private const val UNOPENED_BERICHT_ID = "9e021130-8cbd-4c6f-846a-677448e21ce8"
        private const val UNKNOWN_BERICHT_ID = "00000000-0000-0000-0000-000000000001"
        private const val KNOWN_DOC_ID = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"
        private const val UNRELATED_DOC_ID = "00000000-0000-0000-0000-0000000000ff"
    }
}