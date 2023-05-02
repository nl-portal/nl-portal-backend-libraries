/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
package com.ritense.portal.graphql.security

import com.ritense.portal.graphql.BaseIntegrationTest
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.function.Consumer

class AuthenticationIT(
    @Autowired private val testClient: WebTestClient
) : BaseIntegrationTest() {

    val GRAPHQL_ENDPOINT = "/graphql"
    val GRAPHQL_MEDIA_TYPE = MediaType("application", "graphql")
    val DATA_JSON_PATH = "$.data"
    val ERRORS_JSON_PATH = "$.errors"
    val EXTENSIONS_JSON_PATH = "$.extensions"

    @Test
    @WithMockUser("some-user-id")
    fun `authorized query works with authorization`() {
        val query = "getAuthenticated"
        val body =
            "query {\n" +
                "    $query \n" +
                "}"

        testClient.post()
            .uri(GRAPHQL_ENDPOINT)
            .accept(APPLICATION_JSON)
            .contentType(GRAPHQL_MEDIA_TYPE)
            .bodyValue(body)
            .exchange()
            .verifyOnlyDataExists(query)
            .jsonPath("$DATA_JSON_PATH.$query").isEqualTo("authenticated")
    }

    @Test
    fun `authorized query doesnt work without authorization`() {
        val query = "getAuthenticated"
        val body =
            "query {\n" +
                "    $query \n" +
                "}"

        testClient.post()
            .uri(GRAPHQL_ENDPOINT)
            .accept(APPLICATION_JSON)
            .contentType(GRAPHQL_MEDIA_TYPE)
            .bodyValue(body)
            .exchange()
            .verifyOnlyErrorExists(query)
    }

    @Test
    fun `unauthorized query works without authorization`() {
        val query = "getUnauthenticated"
        val body =
            "query {\n" +
                "    $query \n" +
                "}"

        testClient.post()
            .uri(GRAPHQL_ENDPOINT)
            .accept(APPLICATION_JSON)
            .contentType(GRAPHQL_MEDIA_TYPE)
            .bodyValue(body)
            .exchange()
            .verifyOnlyDataExists(query)
            .jsonPath("$DATA_JSON_PATH.$query").isEqualTo("unauthenticated")
    }

    fun WebTestClient.ResponseSpec.verifyOnlyDataExists(expectedQuery: String): WebTestClient.BodyContentSpec {
        return this.expectBody()
            .consumeWith(Consumer { t -> logger.info { t } })
            .jsonPath("$DATA_JSON_PATH.$expectedQuery").exists()
            .jsonPath(ERRORS_JSON_PATH).doesNotExist()
            .jsonPath(EXTENSIONS_JSON_PATH).doesNotExist()
    }

    fun WebTestClient.ResponseSpec.verifyOnlyErrorExists(expectedQuery: String): WebTestClient.BodyContentSpec {
        return this.expectBody()
            .consumeWith(Consumer { t -> logger.info { t } })
            .jsonPath(ERRORS_JSON_PATH).exists()
            .jsonPath(DATA_JSON_PATH).doesNotExist()
            .jsonPath(EXTENSIONS_JSON_PATH).doesNotExist()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}