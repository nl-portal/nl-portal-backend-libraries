/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.haalcentraal.client.tokenexchange

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.net.URI
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class KeyCloakUserTokenExchangeFilter(
    private val webClient: WebClient,
    private val targetAudience: String
) : UserTokenExchangeFilter {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        if (request.headers()[HttpHeaders.AUTHORIZATION].isNullOrEmpty()) {
            val accessToken = exchangeToken()?.accessToken
            if (accessToken != null) {
                val r = ClientRequest.from(request)
                    .headers { headers -> headers.setBearerAuth(accessToken) }
                    .build()
                return next.exchange(r)
            }
        } else {
            logger.debug { "${HttpHeaders.AUTHORIZATION} was already set. Skipping user token exchange." }
        }

        return next.exchange(request)
    }

    private fun exchangeToken(): TokenResponse? {

        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is JwtAuthenticationToken) {
            val currentToken = authentication.token

            return webClient.post()
                .uri(URI.create("${currentToken.issuer.toString().trimEnd('/')}/protocol/openid-connect/token"))
                .body(
                    BodyInserters.fromFormData(
                        LinkedMultiValueMap<String, String>()
                            .apply {
                                add("client_id", currentToken.getClaim("azp"))
                                add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                                add("subject_token", currentToken.tokenValue)
                                add("requested_token_type", "urn:ietf:params:oauth:token-type:access_token")
                                add("audience", targetAudience)
                            }
                    )
                )
                .retrieve()
                .bodyToMono<TokenResponse>()
                .block()
        }

        return null
    }

    data class TokenResponse(@JsonValue @JsonProperty("access_token") val accessToken: String)

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}