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
package nl.nlportal.verificatie.graphql

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.test.runTest
import nl.nlportal.commonground.authentication.WithBurgerUser
import nl.nlportal.verificatie.TestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.HttpGraphQlTester

@SpringBootTest
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class VerificatieQueryIT(
    @Autowired private val httpGraphQlTester: HttpGraphQlTester,
) {
    @Test
    @WithBurgerUser("569312863")
    fun `verificatie config`() =
        runTest {
            val responseBody =
                httpGraphQlTester
                    .document(TestHelper.readFileAsString("/config/graphql/verificatieConfig.gql"))
                    .execute()
                    .errors()
                    .verify()
                    .path("verificatieConfig")
                    .entity(JsonNode::class.java)
                    .get()

            assertEquals(true, responseBody.get("enabled").booleanValue())
            assertEquals("[\"EMAIL\",\"TELEFOONNUMMER\"]", responseBody.get("typesNeedVerification").toString())
        }
}