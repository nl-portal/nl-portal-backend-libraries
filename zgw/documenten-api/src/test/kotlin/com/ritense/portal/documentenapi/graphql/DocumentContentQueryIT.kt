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
package com.ritense.portal.documentenapi.graphql

import com.ritense.portal.documentenapi.client.DocumentApisConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.source
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import java.io.InputStream
import java.util.Base64

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(PER_CLASS)
internal class DocumentContentQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private var documentApisConfig: DocumentApisConfig
) {
    lateinit var server1: MockWebServer
    lateinit var server2: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server1 = setupMockOpenZaakServer("logo.png")
        server2 = setupMockOpenZaakServer("github.png")
        documentApisConfig.documentapis.get(0).url = server1.url("/").toString()
        documentApisConfig.documentapis.get(1).url = server2.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server1.shutdown()
        server2.shutdown()
    }

    @Test
    @WithMockUser("test")
    fun getDocumentContentServer1() {
        val documentApi = documentApisConfig.getConfig(server1.url("/").toString())
        val query = """
            query {
                getDocumentContent(id :"095be615-a8ad-4c33-8e9c-c7612fbf6c9f",documentApi : "${documentApi.url}") {
                    content
                }
            }
        """.trimIndent()

        val basePath = "$.data.getDocumentContent"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.content").isEqualTo(
                getResourceAsStream("logo.png").use { Base64.getEncoder().encodeToString(it.readAllBytes()) }
            )
    }

    @Test
    @WithMockUser("test")
    fun getDocumentContentServer2() {
        val documentApi = documentApisConfig.getConfig(server2.url("/").toString())
        val query = """
            query {
                getDocumentContent(id :"095be615-a8ad-4c33-8e9c-c7612fbf6c9f",documentApi : "${documentApi.url}") {
                    content
                }
            }
        """.trimIndent()

        val basePath = "$.data.getDocumentContent"

        testClient.post()
            .uri("/graphql")
            .accept(APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .jsonPath(basePath).exists()
            .jsonPath("$basePath.content").isEqualTo(
                getResourceAsStream("github.png").use { Base64.getEncoder().encodeToString(it.readAllBytes()) }
            )
    }

    fun setupMockOpenZaakServer(resource: String): MockWebServer {
        val server = MockWebServer()
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path?.substringBefore('?')
                val response = when (path) {
                    "/documenten/api/v1/enkelvoudiginformatieobjecten/095be615-a8ad-4c33-8e9c-c7612fbf6c9f/download"
                    -> handleDocumentRequest(resource)

                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
        server.start()
        return server
    }

    fun handleDocumentRequest(resource: String): MockResponse {
        val body = Buffer().apply { writeAll(getResourceAsStream(resource).source()) }
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
}