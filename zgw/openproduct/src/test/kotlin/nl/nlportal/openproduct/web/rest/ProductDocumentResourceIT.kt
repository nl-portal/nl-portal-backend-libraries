/*
 * Copyright 2026 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.web.rest

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URI
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.openproduct.TestHelper
import nl.nlportal.openproduct.autoconfigure.OpenProductModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductDocumentResourceIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val openProductModuleConfiguration: OpenProductModuleConfiguration,
    @Autowired private val documentApisConfig: DocumentApisConfig,
) {
    companion object {
        private const val KNOWN_DOC_ID = "095be615-a8ad-4c33-8e9c-c7612fbf6c9f"
        private const val MISCONFIGURED_DOC_ID = "f977e33f-16ae-4e55-9328-eab97bb8297d"
        private const val UNRELATED_DOC_ID = "00000000-0000-0000-0000-0000000000ff"
        private val logger = KotlinLogging.logger {}

        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.openproduct.properties.product-api-url") { url }
            propsRegistry.add("nl-portal.config.openproduct.properties.product-type-api-url") { url }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start(9000)
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
        openProductModuleConfiguration.properties.productApiUrl = URI(url)
        openProductModuleConfiguration.properties.productTypeApiUrl = URI(url)
        documentApisConfig.properties.getConfig("openzaak").url = server?.url("/").toString()
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should stream content with safe Content-Disposition header for valid request`() {
        webTestClient
            .get()
            .uri("/api/product/694242af-d906-470b-b7e1-eb3527886854/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .exists("Content-Disposition")
            .expectHeader()
            .valueMatches("Content-Disposition", "attachment; filename=\".*\"")
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should return 404 when product is not found`() {
        webTestClient
            .get()
            .uri("/api/product/eda96f52-171f-46e4-b7a3-134e3cdd1276/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    @WithBurgerUser("569312864")
    fun `should return 403 when user is not authorized`() {
        webTestClient
            .get()
            .uri("/api/product/694242af-d906-470b-b7e1-eb3527886854/document/$KNOWN_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should return 404 when document is not found as part of product`() {
        webTestClient
            .get()
            .uri("/api/product/694242af-d906-470b-b7e1-eb3527886854/document/$UNRELATED_DOC_ID/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should return 400 when documentapi is not found`() {
        webTestClient
            .get()
            .uri("/api/product/694242af-d906-470b-b7e1-eb3527886854/document/$MISCONFIGURED_DOC_ID/content")
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
                        "GET /producten/694242af-d906-470b-b7e1-eb3527886854" -> {
                            TestHelper.mockResponseFromFile("/config/data/get-product.json")
                        }

                        "GET /enkelvoudiginformatieobjecten/${KNOWN_DOC_ID}" -> {
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