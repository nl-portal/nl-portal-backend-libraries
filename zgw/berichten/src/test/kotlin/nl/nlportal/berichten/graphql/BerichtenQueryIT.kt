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

import kotlinx.coroutines.test.runTest
import nl.nlportal.berichten.TestHelper
import nl.nlportal.berichten.autoconfigure.BerichtenConfigurationProperties
import nl.nlportal.berichten.service.BerichtenService
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.zgw.objectenapi.autoconfiguration.ObjectsApiClientConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.nio.charset.Charset

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BerichtenQueryIT(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val berichtenConfigurationProperties: BerichtenConfigurationProperties,
    @Autowired private val objectsApiClientConfig: ObjectsApiClientConfig,
) {
    @SpyBean lateinit var berichtenService: BerichtenService

    lateinit var mockObjectenApi: MockWebServer

    @BeforeAll
    fun setUp() {
        mockObjectenApi = MockWebServer()
        mockObjectenApi.start()
        objectsApiClientConfig.url = mockObjectenApi.url("/").toUri()
    }

    @AfterAll
    internal fun tearDown() {
        mockObjectenApi.shutdown()
    }

    @Test
    @WithBurgerUser("999990755")
    fun `should return BerichtenPage`() =
        runTest {
            // given
            val objectenResponse =
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(TestHelper.testObjectenResponse)

            with(mockObjectenApi) {
                enqueue(objectenResponse)
            }

            // when
            val response =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromValue(TestHelper.testBerichtenRequest))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val objectenRequest = mockObjectenApi.takeRequest()

            // then
            assertEquals(
                berichtenConfigurationProperties.berichtObjectTypeUrl,
                objectenRequest.requestUrl!!.queryParameter("type"),
            )
            assertEquals(
                "identificatie__type__exact__bsn,identificatie__value__exact__999990755",
                objectenRequest.requestUrl!!.queryParameter("data_attrs"),
            )

            verify(berichtenService, times(1)).getBerichtenPage(any(), any(), any())
            assertEquals(mapper.readTree(TestHelper.testBerichtenResponse), mapper.readTree(response))
        }

    companion object {
        private val mapper = Mapper.get()
    }
}