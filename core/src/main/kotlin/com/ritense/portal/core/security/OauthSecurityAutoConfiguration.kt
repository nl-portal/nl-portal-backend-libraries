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
package com.ritense.portal.core.security

import com.ritense.portal.core.security.config.HttpSecurityConfigurer
import com.ritense.portal.graphql.autoconfigure.CorsPathConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono

@AutoConfiguration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(CorsPathConfiguration::class)
class OauthSecurityAutoConfiguration {

    @Bean
    fun springSecurityWebFilterChain(
        http: ServerHttpSecurity,
        converter: Converter<Jwt, out Mono<out AbstractAuthenticationToken>>?,
        corsPathConfiguration: CorsPathConfiguration,
        securityConfigurers: List<HttpSecurityConfigurer>
    ): SecurityWebFilterChain {
        securityConfigurers.forEach { it.configure(http) }

        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.pathMatchers("/playground").permitAll()
                it.pathMatchers("/graphql").permitAll()
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer {
                it.jwt {
                    it.jwtAuthenticationConverter(converter)
                }
            }
            .build()
    }

    @Bean
    fun corsWebFilter(corsPathConfiguration: CorsPathConfiguration): CorsWebFilter {
        val source = UrlBasedCorsConfigurationSource()

        corsPathConfiguration.cors.forEach {
            source.registerCorsConfiguration(it.path, it.config)
        }

        return CorsWebFilter(source)
    }
}