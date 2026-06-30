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
package nl.nlportal.besluiten.web.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.besluiten.TestHelper
import nl.nlportal.besluiten.client.BesluitenApiConfig
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.documentenapi.client.DocumentApisConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(Lifecycle.PER_METHOD)
class BesluitDocumentResourceIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val documentApisConfig: DocumentApisConfig,
    @Autowired private val besluitenApiConfig: BesluitenApiConfig
) {
    companion object {
        private const val KNOWN_DOC_ID = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"
        private const val BESLUIT_ID = "8863ab83-3496-4f40-9cad-f9d9526597c8"
        private const val UNKNOWN_BESLUIT_ID = "2a27bc3c-6a4c-432a-a9cb-5c31004e7769"
        private const val UNRELATED_DOC_ID = "00000000-0000-0000-0000-0000000000ff"
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.besluitenapi.properties.url") { url }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start(10001)
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
        setupDispatcher()
        url = server?.url("/").toString()
        besluitenApiConfig.properties.url = url
        documentApisConfig.properties.getConfig("openzaak").url = server?.url("/").toString()
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should stream content with safe Content-Disposition header for valid request`() {
        webTestClient
            .get()
            .uri("/api/besluiten/$BESLUIT_ID/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Content-Disposition")
            .expectHeader()
            .valueMatches("Content-Disposition", "attachment; filename=\".*\"")
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return 404 when besluit not found`() {
        webTestClient
            .get()
            .uri("/api/besluiten/$UNKNOWN_BESLUIT_ID/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return 400 when besluit document is not related to besluit`() {
        webTestClient
            .get()
            .uri("/api/besluiten/$BESLUIT_ID/document/$UNRELATED_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    private fun setupDispatcher() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    return when (request.method + " " + path) {
                        "GET /besluiten/api/v1/besluiten/$BESLUIT_ID" -> {
                            TestHelper.mockResponse(TestHelper.handleBesluitRequest)
                        }

                        "GET /besluiten/api/v1/besluitinformatieobjecten/$KNOWN_DOC_ID" -> {
                            TestHelper.mockResponse(TestHelper.handleBesluitDocumentRequest)
                        }

                        "GET /besluiten/api/v1/besluitinformatieobjecten/$UNRELATED_DOC_ID" -> {
                            TestHelper.mockResponse(TestHelper.handleBesluitDocumentUnrelatedRequest)
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
        server?.dispatcher = dispatcher
    }
}
