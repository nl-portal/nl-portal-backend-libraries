/*
 * Copyright 2025 Ritense BV, the Netherlands.
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
package nl.nlportal.core.nl.nlportal.core.frontend

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import nl.nlportal.core.frontend.configuration.FrontendFeaturesConfigurationProperties
import nl.nlportal.core.util.Mapper

@SpringBootTest
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FrontendFeaturesConfigurationResourceTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val frontendFeaturesConfigurationProperties: FrontendFeaturesConfigurationProperties,
) {
    @Test
    fun `get features`() {
        val responseBodyContent =
            webTestClient
                .get()
                .uri("/api/public/features")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()
                .returnResult()
                .responseBody

        val responseJson = Mapper.get().readTree(responseBodyContent)

        assertEquals(frontendFeaturesConfigurationProperties.properties.myAddressResearchUrl, responseJson.requiredAt("/properties/myAddressResearchUrl").textValue())
    }
}