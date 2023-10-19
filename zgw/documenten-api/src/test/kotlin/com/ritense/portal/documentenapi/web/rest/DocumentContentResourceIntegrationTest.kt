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
package com.ritense.portal.documentenapi.web.rest

import com.ritense.portal.commonground.authentication.WithBurgerUser
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.documentenapi.TestHelper
import com.ritense.portal.documentenapi.client.DocumentApiConfig
import com.ritense.portal.documentenapi.client.DocumentApisConfig
import com.ritense.portal.documentenapi.domain.DocumentStatus
import com.ritense.portal.documentenapi.domain.PostEnkelvoudiginformatieobjectRequest
import java.io.InputStream
import java.util.Base64
import java.util.UUID
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.source
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentContentResourceIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val documentApisConfig: DocumentApisConfig
) {
    lateinit var server: MockWebServer
    protected var executedRequests: MutableList<RecordedRequest> = mutableListOf()

    @BeforeAll
    fun setUp() {
        server = MockWebServer()
        setupMockDocumentServer()
        server.start()
        documentApisConfig.getDefault().url = server.url("/").toString()
    }

    @AfterAll
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should download using data streams`() {
        val uuid = UUID.fromString("095be615-a8ad-4c33-8e9c-c7612fbf6c9f")

        // Call rest endpoint with webtestclient
        webTestClient.get()
            .uri("/api/document/{documentId}/documentapi/localhost/content", uuid.toString())
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .expectBody()
            .consumeWith {
                assertTrue(it.responseBody.contentEquals(getResourceAsStream("logo.png").readAllBytes()))
            }
    }

    @Test
    @WithBurgerUser("569312863")
    fun `should upload using data streams`() {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("/data/test-file.txt", this::class.java.classLoader))

        webTestClient.post()
            .uri("/api/document/content")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()

        val requestBody = getRequestBody(HttpMethod.POST, "/documenten/api/v1/enkelvoudiginformatieobjecten", PostEnkelvoudiginformatieobjectRequest::class.java)
        assertThat(requestBody.bronorganisatie).isEqualTo("051845623")
        assertThat(requestBody.creatiedatum).isNotBlank
        assertThat(requestBody.titel).isEqualTo("test-file.txt")
        assertThat(requestBody.auteur).isEqualTo("569312863")
        assertThat(requestBody.status).isEqualTo(DocumentStatus.DEFINITIEF)
        assertThat(requestBody.taal).isEqualTo("nld")
        assertThat(requestBody.bestandsnaam).isEqualTo("test-file.txt")
        assertThat(requestBody.inhoud).isEqualTo(Base64.getEncoder().encodeToString("Test content".toByteArray()))
        assertThat(requestBody.indicatieGebruiksrecht).isFalse
        assertThat(requestBody.informatieobjecttype).isEqualTo("http://localhost:8001/catalogi/api/v1/informatieobjecttypen/00000000-0000-0000-000000000000")
    }

    fun setupMockDocumentServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                executedRequests.add(request)
                val path = request.path?.substringBefore('?')
                val response = when (request.method + " " + path) {
                    "GET /documenten/api/v1/enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f/download"
                    -> handleDocumentContentRequest()

                    "GET /documenten/api/v1/enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f"
                    -> TestHelper.mockResponseFromFile("/data/get-enkelvoudiginformatieobject-response.json")

                    "POST /documenten/api/v1/enkelvoudiginformatieobjecten"
                    -> TestHelper.mockResponseFromFile("/data/post-enkelvoudiginformatieobject-response.json")

                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }

    fun handleDocumentContentRequest(): MockResponse {
        val body = Buffer().apply { writeAll(getResourceAsStream("logo.png").source()) }
        return mockResponse(body)
    }

    fun mockResponse(body: Buffer): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/octet-stream")
            .setResponseCode(200)
            .setBody(body)
    }

    fun getResourceAsStream(resource: String): InputStream {
        return Thread.currentThread().contextClassLoader.getResourceAsStream(resource)!!
    }

    private fun findRequest(method: HttpMethod, path: String): RecordedRequest? {
        return executedRequests
            .filter { method.matches(it.method!!) }
            .firstOrNull { it.path?.substringBefore('?').equals(path) }
    }

    private fun <T> getRequestBody(method: HttpMethod, path: String, clazz: Class<T>): T {
        return Mapper.get().readValue(findRequest(method, path)!!.body.readUtf8(), clazz)
    }
}