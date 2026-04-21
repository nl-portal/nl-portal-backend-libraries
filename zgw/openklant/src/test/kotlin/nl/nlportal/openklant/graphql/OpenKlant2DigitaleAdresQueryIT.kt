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
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openklant.service.OpenKlant2Service
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import nl.nlportal.openklant.TestHelper
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Tag("integration")
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2DigitaleAdresQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
) {
    @MockitoSpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    @Test
    fun `should introspect DigitaleAdres type`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/digitaleAdresIntrospection.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("__type")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            assertEquals("OBJECT", responseBody.get("kind")?.textValue())
            assertEquals("DigitaleAdresResponse", responseBody.get("name")?.textValue())
        }

    @Test
    @WithBurgerUser("296648875")
    fun `should find DigitaleAdressen for authenticated user`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserDigitaleAdressen.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserDigitaleAdressen")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).findDigitaleAdressen(any(), any())

            assertNotNull(responseBody)
            assertEquals("EMAIL", responseBody.get(0)?.get("type")?.textValue())
        }

    @Test
    @WithBurgerUser("111111110")
    fun `should return empty list when no DigitaleAdres was found for authenticated user`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserDigitaleAdressen.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserDigitaleAdressen")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).findDigitaleAdressen(any(), any())
            assertTrue(responseBody.isEmpty)
        }
}