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
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.service.OpenKlant2Service
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Tag("integration")
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2KlantContactQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
) {
    @MockitoSpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    @Test
    @WithBurgerUser("296648875")
    fun `should find KlantContacten for authenticated user`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserKlantContacten.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserKlantContacten")
                    .entity(JsonNode::class.java)
                    .get()

            assertNotNull(responseBody)
            assertEquals("E-mail", responseBody.get(0)?.get("kanaal")?.textValue())
        }

    @Test
    @WithBurgerUser("569312863")
    fun `should find KlantContacten for authenticated user for a zaak`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserKlantContactenForZaak.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserKlantContacten")
                    .entity(JsonNode::class.java)
                    .get()

            assertNotNull(responseBody)
            assertEquals("E-mail", responseBody.get(0)?.get("kanaal")?.textValue())
        }

    @Test
    @WithBurgerUser("123456788")
    fun `should find KlantContact for authenticated user`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserKlantContact.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserKlantContact")
                    .entity(JsonNode::class.java)
                    .get()

            verify(openKlant2Service, times(1)).findKlantContact(any())

            assertNotNull(responseBody)
            assertEquals("E-mail", responseBody?.get("kanaal")?.textValue())
        }
}