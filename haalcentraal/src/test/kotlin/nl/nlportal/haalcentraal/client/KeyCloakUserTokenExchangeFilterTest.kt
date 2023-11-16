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
package nl.nlportal.haalcentraal.client

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.runBlocking
import nl.nlportal.haalcentraal.client.tokenexchange.KeyCloakUserTokenExchangeFilter
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URLDecoder
import java.nio.charset.Charset
import java.time.Instant
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KeyCloakUserTokenExchangeFilterTest {
    private lateinit var server: MockWebServer
    private val signingKey = Keys.hmacShaKeyFor(UUID.randomUUID().toString().toByteArray(Charset.forName("UTF-8")))
    private val exchangeToken = Jwts.builder()
        .setIssuedAt(Date.from(Instant.now()))
        .signWith(signingKey)
        .compact()

    @BeforeEach
    internal fun setUp() {
        server = MockWebServer()
        server.start()

        server.enqueue(
            MockResponse()
                .setBody("""{"access_token":"$exchangeToken"}""")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE),
        )

        server.enqueue(
            MockResponse()
                .setBody("OK")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN),
        )
    }

    @AfterEach
    internal fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should exchange user token for a new one`() {
        val serverPath = server.url("/").toString()
        val userToken = Jwt
            .withTokenValue("token")
            .header("alg", "none")
            .claim("azp", "userClient")
            .issuer(serverPath)
            .build()

        val clientBuilder = WebClient.builder()
            .defaultRequest { spec ->
                spec.attribute(
                    HaalCentraalClientProvider.AUTHENTICATION_ATTRIBUTE_NAME,
                    JwtAuthenticationToken(userToken),
                )
            }
            .baseUrl(serverPath)

        val client = clientBuilder.clone()
            .filter(
                KeyCloakUserTokenExchangeFilter(
                    clientBuilder.clone().build(),
                    "targetClient",
                ),
            )
            .build()

        runBlocking {
            val apiResponseBody = client.get()
                .uri("/")
                .retrieve()
                .bodyToMono<String>()
                .block()

            val tokenRequest = server.takeRequest()
            assertThat(tokenRequest.path).isEqualTo("/protocol/openid-connect/token")
            val apiRequest = server.takeRequest()
            val tokenRequestBody = tokenRequest.body.readUtf8()
            val map = decodeQuery(tokenRequestBody)
            assertThat(map).isNotNull
            assertThat(map["audience"]).contains("targetClient")
            assertThat(map["grant_type"]).contains("urn:ietf:params:oauth:grant-type:token-exchange")
            assertThat(map["subject_token"]).contains("token")
            assertThat(map["client_id"]).contains("userClient")
            assertThat(map["requested_token_type"]).contains("urn:ietf:params:oauth:token-type:access_token")

            assertThat(apiRequest.path).isEqualTo("/")
            assertThat(apiRequest.getHeader(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer $exchangeToken")

            assertThat(apiResponseBody).isEqualTo("OK")
        }
    }

    fun decodeQuery(query: String): Map<String, Set<String?>> {
        val map = HashMap<String, MutableSet<String?>>()
        query.split("&").forEach { param ->
            val split = param.split("=")
            map.getOrPut(split[0]) { mutableSetOf() }.apply {
                split.elementAtOrNull(1)?.let {
                    add(URLDecoder.decode(it, Charsets.UTF_8))
                }
            }
        }
        return map
    }
}