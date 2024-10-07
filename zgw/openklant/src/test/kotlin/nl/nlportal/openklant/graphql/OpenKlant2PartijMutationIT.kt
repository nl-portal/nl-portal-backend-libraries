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
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.graphql.domain.PartijType.PERSOON
import nl.nlportal.openklant.service.OpenKlant2Service
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.nio.charset.Charset

@SpringBootTest
@Tag("integration")
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2PartijMutationIT(
    @Autowired private val webTestClient: WebTestClient,
) {
    @SpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    @Test
    @Order(1)
    @WithBurgerUser("999990755")
    fun `should create Partij for burger`() =
        runTest {
            // when
            val createPartijResponse =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/createPartij.graphql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val createPartijResult =
                objectMapper
                    .readValue<JsonNode>(createPartijResponse!!)
                    .get("data")
                    ?.get("createPartij")
            // then
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())

            assertTrue(createPartijResult is ObjectNode)
            assertEquals(PERSOON.name, createPartijResult!!.requiredAt("/type")?.textValue())
            assertTrue(createPartijResult.requiredAt("/indicatieActief").booleanValue())
            assertTrue(createPartijResult.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Bob de Bouwer",
                createPartijResult.requiredAt("/persoonsIdentificatie/volledigeNaam").textValue(),
            )
            assertEquals(
                "Bob",
                createPartijResult.requiredAt("/persoonsIdentificatie/contactnaam/voornaam").textValue(),
            )
            assertEquals(
                "de",
                createPartijResult.requiredAt("/persoonsIdentificatie/contactnaam/voorvoegselAchternaam").textValue(),
            )
            assertEquals(
                "Bouwer",
                createPartijResult.requiredAt("/persoonsIdentificatie/contactnaam/achternaam").textValue(),
            )
        }

    @Test
    @Order(2)
    @WithBurgerUser("999990755")
    fun `should update existing Partij for burger`() =
        runTest {
            // when
            val updatePartijResponse =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/updatePartij.graphql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val updatePartijResult =
                objectMapper
                    .readValue<JsonNode>(updatePartijResponse!!)
                    .get("data")
                    ?.get("updatePartij")
            // then
            verify(openKlant2Service, times(1)).updatePartij(any(), any())

            assertTrue(updatePartijResult is ObjectNode)
            assertEquals(PERSOON.name, updatePartijResult!!.requiredAt("/type")?.textValue())
            assertTrue(updatePartijResult.requiredAt("/indicatieActief").booleanValue())
            assertFalse(updatePartijResult.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Kees de Boer",
                updatePartijResult.requiredAt("/persoonsIdentificatie/volledigeNaam").textValue(),
            )
            assertEquals(
                "Kees",
                updatePartijResult.requiredAt("/persoonsIdentificatie/contactnaam/voornaam").textValue(),
            )
            assertEquals(
                "de",
                updatePartijResult.requiredAt("/persoonsIdentificatie/contactnaam/voorvoegselAchternaam").textValue(),
            )
            assertEquals(
                "Boer",
                updatePartijResult.requiredAt("/persoonsIdentificatie/contactnaam/achternaam").textValue(),
            )
        }

    @Test
    @Order(3)
    @WithBurgerUser("11111110")
    fun `should create Partij when update fails due to missing Partij`() =
        runTest {
            // when
            val updatePartijResponse =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/updatePartij.graphql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val updatePartijResult =
                objectMapper
                    .readValue<JsonNode>(updatePartijResponse!!)
                    .get("data")
                    ?.get("updatePartij")
            // then
            verify(openKlant2Service, times(1)).updatePartij(any(), any())
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())

            assertTrue(updatePartijResult is ObjectNode)
            assertEquals(PERSOON.name, updatePartijResult!!.requiredAt("/type")?.textValue())
            assertTrue(updatePartijResult.requiredAt("/indicatieActief").booleanValue())
            assertFalse(updatePartijResult.requiredAt("/indicatieGeheimhouding").booleanValue())
            assertEquals(
                "Kees de Boer",
                updatePartijResult.requiredAt("/persoonsIdentificatie/volledigeNaam").textValue(),
            )
            assertEquals(
                "Kees",
                updatePartijResult.requiredAt("/persoonsIdentificatie/contactnaam/voornaam").textValue(),
            )
            assertEquals(
                "de",
                updatePartijResult.requiredAt("/persoonsIdentificatie/contactnaam/voorvoegselAchternaam").textValue(),
            )
            assertEquals(
                "Boer",
                updatePartijResult.requiredAt("/persoonsIdentificatie/contactnaam/achternaam").textValue(),
            )
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}