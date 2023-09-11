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
package nl.nlportal.klant.contactmomenten.graphql

import com.ritense.portal.commonground.authentication.WithBurgerUser
import kotlinx.coroutines.test.runTest
import nl.nlportal.klant.contactmomenten.TestHelper
import nl.nlportal.klant.generiek.client.OpenKlantClientConfig
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
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ContactMomentQueryIT(
    @Autowired private val testClient: WebTestClient,
    @Autowired private val openKlantClientConfig: OpenKlantClientConfig
) {

    lateinit var server: MockWebServer
    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockOpenKlantServer()
        server.start()
        openKlantClientConfig.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBurgerUser("123")
    fun getKlantContactMomenten() = runTest {
        val query = """
            query {
                getKlantContactMomenten(klant: "dummy") {
                    content {
                        registratiedatum
                        tekst
                        kanaal
                    }
                }
            }
        """.trimIndent()

        val basePath = "$.data.getKlantContactMomenten"
        val resultPath = "$basePath.content[0]"

        val response = testClient.post()
            .uri("/graphql")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType("application", "graphql"))
            .bodyValue(query)
            .exchange()
            .expectBody()
            .consumeWith(System.out::println)

        response
            .jsonPath(basePath).exists()
            .jsonPath("$resultPath.tekst").isEqualTo("Contact moment")
            .jsonPath("$resultPath.kanaal").isEqualTo("mail")
            .jsonPath("$resultPath.registratiedatum").isEqualTo("2019-08-24T14:15:22Z")
    }

    fun setupMockOpenKlantServer() {
        val dispatcher: Dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val response = when (request.path?.substringBefore('?')) {
                    "/contactmomenten/api/v1/contactmomenten" -> TestHelper.mockResponseFromFile("/data/get-contactmomenten-list-response.json")
                    else -> MockResponse().setResponseCode(404)
                }
                return response
            }
        }
        server.dispatcher = dispatcher
    }
}