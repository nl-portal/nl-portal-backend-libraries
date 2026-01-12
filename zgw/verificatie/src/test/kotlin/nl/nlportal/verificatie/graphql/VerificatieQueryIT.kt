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
package nl.nlportal.verificatie.graphql

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.verificatie.TestHelper
import nl.nlportal.verificatie.autoconfigure.VerificatieModuleConfiguration
import okhttp3.mockwebserver.MockWebServer
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
class VerificatieQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
    @Autowired private val verificatieModuleConfiguration: VerificatieModuleConfiguration,
) {
    companion object {
        @JvmStatic
        var server: MockWebServer? = null

        @JvmStatic
        var url: String = ""

        @JvmStatic
        @DynamicPropertySource
        fun properties(propsRegistry: DynamicPropertyRegistry) {
            propsRegistry.add("nl-portal.config.verificatie.enabled") { true }
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
        url = server?.url("/").toString()
        verificatieModuleConfiguration.enabled = true
    }

    @Test
    @WithBurgerUser("569312863")
    fun `is verificatie Enabled`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/verificatieEnabled.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("verificatieEnabled")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals(true, responseBody.booleanValue())
        }
}