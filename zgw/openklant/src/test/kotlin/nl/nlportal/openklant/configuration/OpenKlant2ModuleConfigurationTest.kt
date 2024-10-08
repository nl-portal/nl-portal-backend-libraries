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
package nl.nlportal.openklant.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.test.runTest
import nl.nlportal.core.util.Mapper
import nl.nlportal.openklant.autoconfigure.OpenKlantModuleConfiguration
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.nio.charset.Charset

@SpringBootTest
@ActiveProfiles("openklant-disabled")
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenKlant2ModuleConfigurationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val openKlantModuleConfiguration: OpenKlantModuleConfiguration,
) {
    @Test
    fun `should not expose openklant type when module is disabled`() =
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
                    .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/partijIntrospection.gql")))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .returnResult()
                    .responseBodyContent
                    ?.toString(Charset.defaultCharset())

            val typeResponse =
                objectMapper
                    .readValue<JsonNode>(responseBodyContent!!)
                    .get("data")
                    ?.get("__type")

            // then
            assertFalse(openKlantModuleConfiguration.enabled)
            assertTrue(typeResponse is NullNode)
        }

    companion object {
        private val objectMapper = Mapper.get()
    }
}