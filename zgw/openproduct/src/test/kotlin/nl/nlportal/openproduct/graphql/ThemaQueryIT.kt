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
package nl.nlportal.openproduct.graphql

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.test.runTest
import nl.nlportal.core.util.Mapper
import nl.nlportal.openproduct.service.OpenProductService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.nio.charset.Charset

@SpringBootTest
@Tag("integration")
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ThemaQueryIT(
    @Autowired private val webTestClient: WebTestClient,
) {
    @MockitoSpyBean
    lateinit var openProductService: OpenProductService

    // @Test
    fun `get themas`() =
        runTest {
            // when
            val responseBodyContent =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getThemas.gql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            logger.info { "RESPONSE => " + responseBodyContent }

            val typeResponse =
                objectMapper
                    .readValue<JsonNode>(responseBodyContent!!)
                    .get("data")
                    ?.get("__type")

            // then
            assertEquals("OBJECT", typeResponse?.get("kind")?.textValue())
            assertEquals("PartijResponse", typeResponse?.get("code")?.textValue())
        }

    // @Test
    fun `get thema`() =
        runTest {
            // when
            val responseBodyContent =
                webTestClient
                    .post()
                    .uri { builder ->
                        builder
                            .path("/graphql")
                            .build()
                    }
                    .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getThema.gql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            logger.info { "RESPONSE => " + responseBodyContent }

            val typeResponse =
                objectMapper
                    .readValue<JsonNode>(responseBodyContent!!)
                    .get("data")
                    ?.get("__type")

            // then
            assertEquals("OBJECT", typeResponse?.get("kind")?.textValue())
            assertEquals("PartijResponse", typeResponse?.get("code")?.textValue())
        }

    companion object {
        private val objectMapper = Mapper.get()
        val logger = KotlinLogging.logger {}
    }
}