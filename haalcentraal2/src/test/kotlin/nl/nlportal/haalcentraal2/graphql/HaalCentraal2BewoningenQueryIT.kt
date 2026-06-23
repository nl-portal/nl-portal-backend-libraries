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
package nl.nlportal.haalcentraal2.graphql

import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.haalcentraal2.TestHelper
import nl.nlportal.haalcentraal2.autoconfiguration.HaalCentraal2ModuleConfiguration
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class HaalCentraal2BewoningenQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val haalCentraal2ModuleConfiguration: HaalCentraal2ModuleConfiguration,
) {
    companion object {
        private val passThroughHeaders =
            mapOf(
                "x-origin-oin" to "test-origin-oin",
                "x-doelbinding" to "test-doelbinding",
                "x-verwerking" to "test-verwerking",
                "x-gebruiker" to "test-gebruiker",
            )

        private val logger: KLogger = KotlinLogging.logger {}
        private val objectMapper = Mapper.get()

        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.haalcentraal2.properties.bewoning-api-url") { url }
            passThroughHeaders.forEach { (name, value) ->
                propsRegistry.add("nl-portal.config.haalcentraal2.properties.additional-headers.$name") { value }
            }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            server = MockWebServer()
            server?.start()
            url = server?.url("/bewoning").toString()
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
        haalCentraal2ModuleConfiguration.properties.bewoningApiUrl = url
    }

    @Test
    @WithBurgerUser("999993872")
    fun getBewonersAantal() {
        val query =
            """
            query {
                getBewonersAantal(adresseerbaarObjectIdentificatie: "0226010000038820", woonplaats: "'s-Gravenhage")
            }
            """.trimIndent()

        val responseBody =
            httpGraphQlTester
                .document(query)
                .execute()
                .errors()
                .verify()
                .path("getBewonersAantal")
                .entity(JsonNode::class.java)
                .get()

        assertEquals(4, responseBody.intValue())
        TestHelper.assertRequestHasPassThroughHeader(server, "/bewoning/bewoningen", passThroughHeaders)
    }

    @Test
    @WithBurgerUser("999993872")
    fun getBewonersAantalNotAllowed() {
        val query =
            """
            query {
                getBewonersAantal(adresseerbaarObjectIdentificatie: "0226010000038820", woonplaats: "Amsterdam")
            }
            """.trimIndent()

        httpGraphQlTester
            .document(query)
            .execute()
            .errors()
            .verify()
            .path("getBewonersAantalV2")
    }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val path = request.path?.substringBefore('?')
                    val response =
                        when (request.method + " " + path) {
                            "POST /bewoning/bewoningen" -> TestHelper.mockResponseFromFile("/data/get-bewoningen.json")
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server?.dispatcher = dispatcher
    }
}