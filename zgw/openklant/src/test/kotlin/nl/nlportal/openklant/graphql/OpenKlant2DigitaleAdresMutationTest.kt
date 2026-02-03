/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URI
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.graphql.domain.DigitaleAdresType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
import nl.nlportal.verificatie.autoconfigure.VerificatieModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2DigitaleAdresMutationTest(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val verificatieModuleConfiguration: VerificatieModuleConfiguration,
    @Autowired private val openKlantModuleConfiguration: OpenKlantModuleConfiguration,
) {
    companion object {
        private val logger: KLogger = KotlinLogging.logger {}

        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.verificatie.properties.url") { url }
            propsRegistry.add("nl-portal.config.openklant2.properties.klantinteracties-api-url") { url }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start()
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
        setupMockServer()
        url = server?.url("/").toString()
        verificatieModuleConfiguration.properties.url = URI(url)
        openKlantModuleConfiguration.properties.klantinteractiesApiUrl = URI(url)
    }

    // @Test
    @WithBurgerUser("296648875")
    fun `should create DigitaleAdres for burger`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/createUserDigitaleAdres.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("createUserDigitaleAdres")
                    .entity(JsonNode::class.java)
                    .get()

            assertTrue(responseBody is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, responseBody.get("type").textValue())
            assertEquals("0611111111", responseBody.get("waarde").textValue())
            assertEquals("Privè telefoonnummer", responseBody.get("omschrijving").textValue())
        }

    // @Test
    @WithBurgerUser("296648875")
    fun `should update existing DigitaleAdres for burger`() =
        runTest {
            // when
            val mutation =
                """
                mutation {
                            updateUserDigitaleAdres(
                                digitaleAdresRequest: {
                                    waarde: "0611111112", 
                                    type: TELEFOONNUMMER, 
                                    omschrijving: "Modified", 
                                    uuid: "f1ad891f-6257-49bb-9b40-6eb9ffa98dfa",
                                    verificatieCode: "1234"
                                }
                            ) {
                                uuid
                                waarde
                                type
                                omschrijving
                            }
                        }
                """.trimIndent()
            val responseBody =
                httpGraphQlTester
                    .document(mutation)
                    .execute()
                    .errors()
                    .verify()
                    .path("updateUserDigitaleAdres")
                    .entity(JsonNode::class.java)
                    .get()

            logger.info { "#####" }
            logger.info { Mapper.get().writeValueAsString(responseBody) }
            logger.info { "#####" }

            assertTrue(responseBody is ObjectNode)
            assertTrue(responseBody is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, responseBody.get("type").textValue())
            assertEquals("0611111112", responseBody.get("waarde").textValue())
            assertEquals("Modified", responseBody.get("omschrijving").textValue())
        }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "POST /api/verification-requests" -> {
                                TestHelper.mockResponseFromFile("/config/data/create-verificatie-response.json")
                            }
                            "POST /api/verification-requests/verify" -> {
                                TestHelper.mockResponseFromFile("/config/data/verify-verificatie-response.json")
                            }
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}