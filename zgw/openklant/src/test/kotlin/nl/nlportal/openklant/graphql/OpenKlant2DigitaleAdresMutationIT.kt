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
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
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
@TestMethodOrder(OrderAnnotation::class)
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenKlant2DigitaleAdresMutationIT(
    @Autowired private val webTestClient: WebTestClient,
) {
    @SpyBean
    lateinit var openKlant2Service: OpenKlant2Service

    lateinit var testdigitaleAdresUUID: String

    @Test
    @Order(1)
    @WithBurgerUser("123456788")
    fun `should create DigitaleAdres for burger`() =
        runTest {
            // when
            val createResponse =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/createUserDigitaleAdres.gql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val createResult =
                objectMapper
                    .readValue<JsonNode>(createResponse!!)
                    .get("data")
                    ?.get("createUserDigitaleAdres")
            // then
            verify(openKlant2Service, times(1)).createDigitaleAdres(any(), any())

            assertTrue(createResult is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, createResult!!.get("type").textValue())
            assertEquals("0611111111", createResult.get("waarde").textValue())
            assertEquals("Privè telefoonnummer", createResult.get("omschrijving").textValue())

            testdigitaleAdresUUID = createResult.get("uuid").textValue()
        }

    @Test
    @Order(2)
    @WithBurgerUser("123456788")
    fun `should update existing DigitaleAdres for burger`() =
        runTest {
            // when
            val createResponse =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .bodyValue(
                        """
                        mutation {
                            updateUserDigitaleAdres(
                                digitaleAdresId: "$testdigitaleAdresUUID",
                                digitaleAdresRequest: {
                                    waarde: "0611111112", type: TELEFOONNUMMER, omschrijving: "Modified"
                                }
                            ) {
                                uuid
                                waarde
                                type
                                omschrijving
                            }
                        }
                        """.trimIndent(),
                    )
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val createResult =
                objectMapper
                    .readValue<JsonNode>(createResponse!!)
                    .get("data")
                    ?.get("updateUserDigitaleAdres")

            // then
            verify(openKlant2Service, times(1)).updateDigitaleAdresById(any(), any(), any())

            assertTrue(createResult is ObjectNode)
            assertTrue(createResult is ObjectNode)
            assertEquals(DigitaleAdresType.TELEFOONNUMMER.name, createResult!!.get("type").textValue())
            assertEquals("0611111112", createResult.get("waarde").textValue())
            assertEquals("Modified", createResult.get("omschrijving").textValue())
        }

    @Test
    @Order(3)
    @WithBurgerUser("123456788")
    fun `should delete existing DigitaleAdres for burger`() =
        runTest {
            // when
            val createResponse =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .bodyValue(
                        """
                        mutation {
                            deleteUserDigitaleAdres(digitaleAdresId: "$testdigitaleAdresUUID")
                        }
                        """.trimIndent(),
                    )
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val createResult =
                objectMapper
                    .readValue<JsonNode>(createResponse!!)
                    .get("data")
                    ?.get("deleteUserDigitaleAdres")

            // then
            verify(openKlant2Service, times(1)).deleteDigitaleAdresById(any(), any())

            assertTrue(createResult is NullNode)

            val userAdressen =
                objectMapper.readTree(
                    webTestClient
                        .post()
                        .uri { builder ->
                            builder
                                .path("/graphql")
                                .build()
                        }
                        .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                        .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getUserDigitaleAdresen.gql")))
                        .exchange()
                        .expectStatus().isOk
                        .expectBody()
                        .returnResult()
                        .responseBodyContent,
                )
                    .get("data")
                    ?.get("getUserDigitaleAdresen")

            assertFalse(testdigitaleAdresUUID in userAdressen!!.mapNotNull { it?.get("uuid")?.textValue() })
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}