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
package nl.nlportal.haalcentraal.hr.graphql

import nl.nlportal.commonground.authentication.WithBedrijfUser
import nl.nlportal.haalcentraal.hr.TestHelper
import nl.nlportal.haalcentraal.hr.client.HaalCentraalHrConfig
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import tools.jackson.databind.JsonNode

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class HandelsregisterQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val haalCentraalClientConfig: HaalCentraalHrConfig,
) {
    lateinit var server: MockWebServer

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        setupMockServer()
        server.start()

        haalCentraalClientConfig.properties.url = server.url("/").toString()
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    @WithBedrijfUser("90012768")
    fun getNaam() {
        val query =
            """
            query {
                getBedrijf {
                    naam
                }
            }
            """.trimIndent()

        val responseBody =
            httpGraphQlTester
                .document(query)
                .execute()
                .errors()
                .verify()
                .path("getBedrijf")
                .entity(JsonNode::class.java)
                .get()

        assertEquals("Test bedrijf", responseBody.get("naam").textValue())
    }

    private fun setupMockServer() {
        val dispatcher: Dispatcher =
            object : Dispatcher() {
                @Throws(InterruptedException::class)
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val response =
                        when (request.path?.substringBefore('?')) {
                            "/basisprofielen/90012768" -> TestHelper.mockResponseFromFile("/data/get-maatschappelijke-activiteiten.json")
                            else -> MockResponse().setResponseCode(404)
                        }
                    return response
                }
            }
        server.dispatcher = dispatcher
    }
}