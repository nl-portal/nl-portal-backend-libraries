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
package nl.nlportal.portal.authentication.service

import nl.nlportal.portal.authentication.exception.UserTypeUnsupportedException
import nl.nlportal.portal.authentication.domain.PortalAuthentication
import nl.nlportal.portal.authentication.domain.SUB_KEY
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import reactor.core.publisher.Mono

open class PortalAuthenticationConverter : Converter<Jwt, Mono<PortalAuthentication>> {
    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): Mono<PortalAuthentication> {
        if (jwt.claims[SUB_KEY] != null) {
            return Mono.just(PortalAuthentication(jwt, jwtGrantedAuthoritiesConverter.convert(jwt)))
        } else {
            throw UserTypeUnsupportedException("User type not supported")
        }
    }
}