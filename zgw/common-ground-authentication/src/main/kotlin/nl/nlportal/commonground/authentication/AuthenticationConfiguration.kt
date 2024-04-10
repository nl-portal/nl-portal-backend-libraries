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

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono

@EnableConfigurationProperties(KeycloakConfig::class)
@AutoConfiguration
class AuthenticationConfiguration {
    @Bean
    fun commonGroundAuthenticationConverter(
        reactiveJwtDecoder: ReactiveJwtDecoder,
        keycloakConfig: KeycloakConfig,
    ): Converter<Jwt, out Mono<out AbstractAuthenticationToken>> {
        return CommonGroundAuthenticationConverter(reactiveJwtDecoder, keycloakConfig)
    }
}