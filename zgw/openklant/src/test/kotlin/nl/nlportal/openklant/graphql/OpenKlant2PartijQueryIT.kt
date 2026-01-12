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
import com.fasterxml.jackson.module.kotlin.treeToValue
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBedrijfUser
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.TestHelper
import nl.nlportal.openklant.client.domain.OrganisatieIdentificatie
import nl.nlportal.openklant.client.domain.PersoonsIdentificatie
import nl.nlportal.openklant.client.domain.SoortPartij
import nl.nlportal.openklant.service.OpenKlant2Service
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2PartijQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
) {
    @MockitoSpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    @Test
    fun `should introspect Partij type`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/partijIntrospection.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("__type")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals("OBJECT", responseBody.get("kind")?.textValue())
            assertEquals("PartijResponse", responseBody.get("name")?.textValue())
        }

    @Test
    @WithBurgerUser("296648875")
    fun `should find Partij for authenticated user`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/findUserPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("findUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).findPartijByAuthentication(any())

            assertNotNull(responseBody)
            assertEquals(SoortPartij.PERSOON.name, responseBody.get("soortPartij")?.textValue())
            assertDoesNotThrow { objectMapper.treeToValue<PersoonsIdentificatie>(responseBody.get("partijIdentificatie")) }
            assertEquals("Lucas Boom", responseBody.requiredAt("/partijIdentificatie/volledigeNaam")?.textValue())
        }

    @Test
    @WithBedrijfUser(
        kvkNummer = "14127293",
    )
    fun `should find Partij for authenticated user as bedrijf`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/findUserPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("findUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).findPartijByAuthentication(any())

            assertNotNull(responseBody)
            assertEquals(SoortPartij.ORGANISATIE.name, responseBody.get("soortPartij")?.textValue())
            assertDoesNotThrow { objectMapper.treeToValue<OrganisatieIdentificatie>(responseBody.get("partijIdentificatie")) }
            assertEquals("Ritense", responseBody.requiredAt("/partijIdentificatie/naam")?.textValue())
        }

    @Test
    @WithBurgerUser("296648875")
    fun `should get Partij by Id for authenticated user`() =
        runTest {
            // when
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/getUserPartij.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("getUserPartij")
                    .entity(JsonNode::class.java)
                    .get()

            // then
            verify(openKlant2Service, times(1)).getPartij(any())

            assertNotNull(responseBody)
            assertEquals(SoortPartij.PERSOON.name, responseBody.get("soortPartij")?.textValue())
            assertDoesNotThrow { objectMapper.treeToValue<PersoonsIdentificatie>(responseBody.get("partijIdentificatie")) }
            assertEquals("Lucas Boom", responseBody.requiredAt("/partijIdentificatie/volledigeNaam")?.textValue())
        }

    @Test
    @WithBurgerUser("99990755")
    fun `should return null when user is not allowed to request Partij`() =
        runTest {
            // when
            httpGraphQlTester
                .document(TestHelper.readFileAsString("/config/graphql/getUserPartij.gql"))
                .execute()
                .errors()
                .verify()
                .path("getUserPartij")

            // then
            verify(openKlant2Service, times(1)).findPartijIdentificatoren(any())
            verify(openKlant2Service, times(0)).getPartij(any())
        }

    @Test
    @WithBurgerUser("111111110")
    fun `should return null when no Partij was found for authenticated user`() =
        runTest {
            // when
            httpGraphQlTester
                .document(TestHelper.readFileAsString("/config/graphql/findUserPartij.gql"))
                .execute()
                .errors()
                .verify()
                .path("findUserPartij")
            verify(openKlant2Service, times(1)).findPartijByAuthentication(any())
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}