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
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.service.OpenKlant2Service
import org.junit.jupiter.api.Disabled
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
    @Disabled
    @WithBurgerUser("999990755")
    fun `should create Partij for authenticated user`() =
        runTest {
            // when
            val responseBody =
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

            val responsePartij =
                objectMapper
                    .readValue<JsonNode>(responseBody!!)
                    .get("data")
                    ?.get("createPartij")

            // then
            verify(openKlant2Service, times(1)).createPartijWithIdentificator(any(), any())
//
//            assertNotNull(responsePartij)
//            assertEquals(SoortPartij.PERSOON.name, responsePartij?.get("soortPartij")?.textValue())
//            assertDoesNotThrow { objectMapper.treeToValue<PersoonsIdentificatie>(responsePartij!!.get("partijIdentificatie")) }
//            assertEquals("Lucas Boom", responsePartij?.requiredAt("/partijIdentificatie/volledigeNaam")?.textValue())
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}