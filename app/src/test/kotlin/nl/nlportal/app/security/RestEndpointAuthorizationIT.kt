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
package nl.nlportal.app.security

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import nl.nlportal.app.TestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureWebTestClient
@TestInstance(PER_CLASS)
@Tag("integration")
class RestEndpointAuthorizationIT {
    @Autowired
    lateinit var webTestClient: WebTestClient

    // ======= AUTHENTICATED ENDPOINTS - Should return 401 without JWT =======

    @Test
    fun `GET document content should require authentication`() {
        webTestClient
            .get()
            .uri("/api/documentapi/openzaak/document/00000000-0000-0000-0000-000000000001/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `POST document upload should require authentication`() {
        webTestClient
            .post()
            .uri("/api/document/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `POST document upload to specific API should require authentication`() {
        webTestClient
            .post()
            .uri("/api/documentapi/openzaak/document/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `GET zaak document content should require authentication`() {
        webTestClient
            .get()
            .uri("/api/zakenapi/zaakdocument/00000000-0000-0000-0000-000000000001/content")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    // ======= PUBLIC ENDPOINTS - Should return 200 without JWT =======

    @Test
    fun `GET public features should be accessible without authentication`() {
        webTestClient
            .get()
            .uri("/api/public/features")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `GET public theme style should be accessible without authentication`() {
        webTestClient
            .get()
            .uri("/api/public/theme/style")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `GET public theme logo should be accessible without authentication`() {
        webTestClient
            .get()
            .uri("/api/public/theme/logo")
            .exchange()
            .expectStatus()
            .isOk
    }

    // ======= ACTUATOR ENDPOINTS - Should be accessible without JWT =======

    @Test
    fun `GET actuator health should be accessible without authentication`() {
        webTestClient
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus()
            .isOk
    }

    // ======= GRAPHQL TRANSPORT - Should be accessible (auth at resolver level) =======

    @Test
    fun `POST graphql endpoint should be accessible without authentication`() {
        webTestClient
            .post()
            .uri("/graphql")
            .header("Content-Type", "application/json")
            .bodyValue("""{"query": "{ __typename }"}""")
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `GET graphiql should be accessible without authentication`() {
        // /graphiql redirects to /graphiql?path=/graphql - a 3xx redirect is still publicly accessible
        webTestClient
            .get()
            .uri("/graphiql")
            .exchange()
            .expectStatus()
            .is3xxRedirection
    }
}