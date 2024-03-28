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
package nl.nlportal.commonground.authentication

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import nl.nlportal.commonground.authentication.exception.UserTypeUnsupportedException
import mu.KotlinLogging
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

class CommonGroundAuthenticationConverter : Converter<Jwt, Mono<CommonGroundAuthentication>> {
    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
    private val webClient = WebClient.create()

    // TODO: remove support for bsn and kvk keys directly in the root of the JWT
    override fun convert(jwt: Jwt): Mono<CommonGroundAuthentication> {
        val exchangedJwt = tokenExchange(jwt)
        val aanvrager = exchangedJwt.claims[AANVRAGER_KEY]
        if (aanvrager is Map<*, *>) {
            if (aanvrager[BSN_KEY] != null) {
                return Mono.just(BurgerAuthentication(exchangedJwt, jwtGrantedAuthoritiesConverter.convert(exchangedJwt)))
            } else if (aanvrager[KVK_NUMMER_KEY] != null) {
                return Mono.just(BedrijfAuthentication(exchangedJwt, jwtGrantedAuthoritiesConverter.convert(exchangedJwt)))
            }
        }

        // This block is for temporary backwards compatibility
        if (jwt.claims.get(BSN_KEY) != null) {
            return Mono.just(BurgerAuthentication(exchangedJwt, jwtGrantedAuthoritiesConverter.convert(exchangedJwt)))
        } else if (jwt.claims.get(KVK_NUMMER_KEY) != null) {
            return Mono.just(BedrijfAuthentication(exchangedJwt, jwtGrantedAuthoritiesConverter.convert(exchangedJwt)))
        }

        val subject = exchangedJwt.subject
        if (subject == null) {
            logger.error { "User with unknown subject has no bsn or kvk nummer assigned" }
        } else {
            logger.error { "User with subject $subject has no bsn or kvk nummer assigned" }
        }
        throw UserTypeUnsupportedException("User type not supported")
    }

    private fun tokenExchange(jwt: Jwt): Jwt {
        val tokenResponse = webClient.post()
            .uri(URI.create("${jwt.issuer.toString().trimEnd('/')}/protocol/openid-connect/token"))
            .body(
                BodyInserters.fromFormData(
                    LinkedMultiValueMap<String, String>()
                        .apply {
                            add("client_id", "NL-portal-backend")
                            add("grant_type", "client_credentials")
                            add("client_secret", "wBdJo9ynONtEFIx9aXrYSHJ7b28W1MEs")
                        },
                ),
            )
            .retrieve()
            .bodyToMono<TokenResponse>()
            .block()

        return Jwt.withTokenValue(tokenResponse!!.accessToken).build()
    }

    data class TokenResponse(
        @JsonValue
        @JsonProperty("access_token")
        val accessToken: String,
    )

    companion object {
        val logger = KotlinLogging.logger {}
    }
}