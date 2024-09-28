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
package nl.nlportal.openklant.graphql

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
import nl.nlportal.openklant.domain.CreatePartij.PersoonsIdentificatie
import nl.nlportal.openklant.domain.SoortPartij
import nl.nlportal.openklant.service.OpenKlant2Service
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.nio.charset.Charset

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PartijQueryIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val openKlantModuleConfiguration: OpenKlantModuleConfiguration,
) {
    @SpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    lateinit var mockOpenKlant: MockWebServer

    @BeforeEach
    fun setUp() {
        mockOpenKlant = MockWebServer()
        mockOpenKlant.start()
        openKlantModuleConfiguration.properties.url = mockOpenKlant.url("/").toUri()
    }

    @AfterEach
    internal fun tearDown() {
        mockOpenKlant.shutdown()
    }

    @Test
    fun `should introspect Partij type`() = runTest {
        // when
        val responseBodyContent =
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }
                .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/partijTypeIntrospection.graphql")))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .returnResult()
                .responseBodyContent
                ?.toString(Charset.defaultCharset())

        val typeResponse =
            objectMapper
                .readValue<JsonNode>(responseBodyContent!!)
                .get("data")
                ?.get("__type")

        // then
        assertEquals("OBJECT", typeResponse?.get("kind")?.textValue())
        assertEquals("Partij", typeResponse?.get("name")?.textValue())
        assertEquals("A Type that represents a Klantinteracties API Partij object", typeResponse?.get("description")?.textValue())
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return Partij for authenticated user`() =
        runTest {
            // given
            val partijenResponse =
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(TestHelper.Partijen.persoonPartijResponse)

            with(mockOpenKlant) {
                enqueue(partijenResponse)
            }

            // when
            val responseBody =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getPartij.graphql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val responsePartij =
                objectMapper
                    .readValue<JsonNode>(responseBody!!)
                    .get("data")
                    ?.get("getPartij")

            val recordedRequest = mockOpenKlant.takeRequest()

            // then
            verify(openKlant2Service, times(1)).findPartij(any())

            assertTrue(recordedRequest.requestUrl.toString().contains("999990755"))
            assertTrue(recordedRequest.requestUrl.toString().contains("persoon"))
            assertNotNull(responsePartij)
            assertEquals(SoortPartij.PERSOON.name, responsePartij?.get("soortPartij")?.textValue())
            assertDoesNotThrow { objectMapper.treeToValue<PersoonsIdentificatie>(responsePartij!!.get("partijIdentificatie")) }
            assertEquals("Lucas Boom", responsePartij?.requiredAt("/partijIdentificatie/volledigeNaam")?.textValue())
        }

    @Test
    @WithBurgerUser("111111110")
    fun `should return null when no Partij was found for authenticated user`() =
        runTest {
            // given
            val partijenResponse =
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(TestHelper.Partijen.emptyPage)

            with(mockOpenKlant) {
                enqueue(partijenResponse)
            }

            // when
            val responseBody =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getPartij.graphql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val responsePartij =
                objectMapper
                    .readValue<JsonNode>(responseBody!!)
                    .get("data")
                    ?.get("getPartij")

            val recordedRequest = mockOpenKlant.takeRequest()

            // then
            verify(openKlant2Service, times(1)).findPartij(any())

            assertTrue(recordedRequest.requestUrl.toString().contains("111111110"))
            assertTrue(recordedRequest.requestUrl.toString().contains("persoon"))
            assertTrue(responsePartij!!.isNull)
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}