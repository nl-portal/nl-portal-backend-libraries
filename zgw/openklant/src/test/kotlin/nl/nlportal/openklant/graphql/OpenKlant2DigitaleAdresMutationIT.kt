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

import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ObjectNode
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openklant.graphql.domain.DigitaleAdresType
import nl.nlportal.openklant.service.OpenKlant2Service
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import nl.nlportal.openklant.TestHelper
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Tag("integration")
@TestMethodOrder(OrderAnnotation::class)
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenKlant2DigitaleAdresMutationIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
) {
    @MockitoSpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    lateinit var testdigitaleAdresUUID: String

    @Test
    @Order(1)
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

            // then
            verify(openKlant2Service, times(1)).createDigitaleAdres(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, responseBody.get("type").textValue())
            assertEquals("0611111111", responseBody.get("waarde").textValue())
            assertEquals("Privè telefoonnummer", responseBody.get("omschrijving").textValue())

            testdigitaleAdresUUID = responseBody.get("uuid").textValue()
        }

    @Test
    @Order(2)
    @WithBurgerUser("296648875")
    fun `should update existing DigitaleAdres for burger`() =
        runTest {
            // when
            val mutation =
                """
                mutation {
                            updateUserDigitaleAdres(
                                digitaleAdresRequest: {
                                    waarde: "0611111112", type: TELEFOONNUMMER, omschrijving: "Modified", uuid: "$testdigitaleAdresUUID"
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

            // then
            verify(openKlant2Service, times(1)).updateDigitaleAdresById(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertTrue(responseBody is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, responseBody.get("type").textValue())
            assertEquals("0611111112", responseBody.get("waarde").textValue())
            assertEquals("Modified", responseBody.get("omschrijving").textValue())
        }

    @Test
    @Order(3)
    @WithBurgerUser("296648875")
    fun `should delete existing DigitaleAdres for burger`() =
        runTest {
            // when
            val mutation =
                """
                mutation {
                            deleteUserDigitaleAdres(digitaleAdresId: "$testdigitaleAdresUUID")
                        }
                """.trimIndent()
            httpGraphQlTester
                .document(mutation)
                .executeAndVerify()

            // then
            verify(openKlant2Service, times(1)).deleteDigitaleAdresById(any(), any())

            val responseBodyUserAdressen =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserDigitaleAdressen.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserDigitaleAdressen")
                    .entity(JsonNode::class.java)
                    .get()

            assertFalse(testdigitaleAdresUUID in responseBodyUserAdressen.mapNotNull { it?.get("uuid")?.textValue() })
        }

    @Test
    @Order(4)
    @WithBurgerUser("395823511")
    fun `should create DigitaleAdres for burger zonder partij`() =
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

            // then
            verify(openKlant2Service, times(1)).createDigitaleAdres(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, responseBody.get("type").textValue())
            assertEquals("0611111111", responseBody.get("waarde").textValue())
            assertEquals("Privè telefoonnummer", responseBody.get("omschrijving").textValue())
        }
}