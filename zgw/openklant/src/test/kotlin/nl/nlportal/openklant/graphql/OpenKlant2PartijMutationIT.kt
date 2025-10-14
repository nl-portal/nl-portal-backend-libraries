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
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBedrijfUser
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.graphql.domain.PartijType.ORGANISATIE
import nl.nlportal.openklant.graphql.domain.PartijType.PERSOON
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
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@Tag("integration")
@TestMethodOrder(OrderAnnotation::class)
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenKlant2PartijMutationIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
) {
    @MockitoSpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    @Test
    @Order(1)
    @WithBurgerUser("999990755")
    fun `should create Partij for burger`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/createUserPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("createUserPartij")
                    .entity(JsonNode::class.java)
                    .get()
            // then
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(PERSOON.name, responseBody.requiredAt("/type")?.textValue())
            assertTrue(responseBody.requiredAt("/indicatieActief").booleanValue())
            assertTrue(responseBody.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Bob de Bouwer",
                responseBody.requiredAt("/persoonsIdentificatie/volledigeNaam").textValue(),
            )
            assertEquals(
                "Bob",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/voornaam").textValue(),
            )
            assertEquals(
                "de",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/voorvoegselAchternaam").textValue(),
            )
            assertEquals(
                "Bouwer",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/achternaam").textValue(),
            )
        }

    @Test
    @Order(2)
    @WithBurgerUser("999990755")
    fun `should update existing Partij for burger`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/updateUserPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("updateUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).updatePartij(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(PERSOON.name, responseBody.requiredAt("/type")?.textValue())
            assertTrue(responseBody.requiredAt("/indicatieActief").booleanValue())
            assertFalse(responseBody.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Kees de Boer",
                responseBody.requiredAt("/persoonsIdentificatie/volledigeNaam").textValue(),
            )
            assertEquals(
                "Kees",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/voornaam").textValue(),
            )
            assertEquals(
                "de",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/voorvoegselAchternaam").textValue(),
            )
            assertEquals(
                "Boer",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/achternaam").textValue(),
            )
        }

    @Test
    @Order(3)
    @WithBurgerUser("483485871")
    fun `should create Partij when update fails due to missing Partij`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/updateUserPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("updateUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).updatePartij(any(), any())
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(PERSOON.name, responseBody.requiredAt("/type")?.textValue())
            assertTrue(responseBody.requiredAt("/indicatieActief").booleanValue())
            assertFalse(responseBody.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Kees de Boer",
                responseBody.requiredAt("/persoonsIdentificatie/volledigeNaam").textValue(),
            )
            assertEquals(
                "Kees",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/voornaam").textValue(),
            )
            assertEquals(
                "de",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/voorvoegselAchternaam").textValue(),
            )
            assertEquals(
                "Boer",
                responseBody.requiredAt("/persoonsIdentificatie/contactnaam/achternaam").textValue(),
            )
        }

    @Test
    @Order(4)
    @WithBedrijfUser(
        kvkNummer = "69599084",
    )
    fun `should create Partij for bedrijf`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/createBedrijfPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("createUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(ORGANISATIE.name, responseBody.requiredAt("/type")?.textValue())
            assertTrue(responseBody.requiredAt("/indicatieActief").booleanValue())
            assertTrue(responseBody.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Gemeente Den Haag",
                responseBody.requiredAt("/organisatieIdentificatie/naam").textValue(),
            )
        }

    @Test
    @Order(5)
    @WithBedrijfUser(
        kvkNummer = "68727720",
        vestigingsNummer = "000037143557",
    )
    fun `should create Partij for bedrijf and vestigingsnummer`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/createBedrijfPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("createUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())

            assertTrue(responseBody is ObjectNode)
            assertEquals(ORGANISATIE.name, responseBody.requiredAt("/type")?.textValue())
            assertTrue(responseBody.requiredAt("/indicatieActief").booleanValue())
            assertTrue(responseBody.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Gemeente Den Haag",
                responseBody.requiredAt("/organisatieIdentificatie/naam").textValue(),
            )
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}