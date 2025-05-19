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

import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.openproduct.TestHelper.verifyOnlyDataExists
import nl.nlportal.openproduct.service.OpenProductService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
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

@SpringBootTest
@Tag("integration")
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OpenProductThemaQueryIT2(
    @Autowired private val webTestClient: WebTestClient,
) {
    @MockitoSpyBean
    lateinit var openProductService: OpenProductService

    @Test
    @WithBurgerUser("569312863")
    fun `get themas`() =
        runTest {
            val basePath = "$.data.getOpenProductThemas"
            val resultPath = "$basePath.content[1]"
            webTestClient
                .post()
                .uri { builder ->
                    builder
                        .path("/graphql")
                        .build()
                }
                .header(HttpHeaders.CONTENT_TYPE, MediaType("application", "graphql").toString())
                .body(BodyInserters.fromResource(ClassPathResource("/config/graphql/getOpenProductThemas.gql")))
                .exchange()
                .verifyOnlyDataExists(basePath)
                .jsonPath("$basePath.totalElements").isEqualTo(4)
                .jsonPath("$resultPath.naam").isEqualTo("Parkeren")
                .jsonPath("$resultPath.producttypen[0].uniformeProductNaam").isEqualTo("UPL-naam nog niet beschikbaar")
                .jsonPath("$resultPath.producttypen[0].code").isEqualTo("PARKEREN")
        }
}