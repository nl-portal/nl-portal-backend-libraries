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

import nl.nlportal.commonground.authentication.exception.UserTypeUnsupportedException
import mu.KotlinLogging
import org.springframework.core.convert.converter.Converter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import reactor.core.publisher.Mono

class CommonGroundAuthenticationConverter : Converter<Jwt, Mono<CommonGroundAuthentication>> {
    private val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    // TODO: remove support for bsn and kvk keys directly in the root of the JWT
    override fun convert(jwt: Jwt): Mono<CommonGroundAuthentication> {
        val aanvrager = jwt.claims[AANVRAGER_KEY]
        if (aanvrager is Map<*, *>) {
            if (aanvrager[BSN_KEY] != null) {
                return Mono.just(BurgerAuthentication(jwt, jwtGrantedAuthoritiesConverter.convert(jwt)))
            } else if (aanvrager[KVK_NUMMER_KEY] != null) {
                return Mono.just(BedrijfAuthentication(jwt, jwtGrantedAuthoritiesConverter.convert(jwt)))
            }
        }

        // This block is for temporary backwards compatibility
        if (jwt.claims.get(BSN_KEY) != null) {
            return Mono.just(BurgerAuthentication(jwt, jwtGrantedAuthoritiesConverter.convert(jwt)))
        } else if (jwt.claims.get(KVK_NUMMER_KEY) != null) {
            return Mono.just(BedrijfAuthentication(jwt, jwtGrantedAuthoritiesConverter.convert(jwt)))
        }

        val subject = jwt.subject
        if (subject == null) {
            logger.error { "User with unknown subject has no bsn or kvk nummer assigned" }
        } else {
            logger.error { "User with subject $subject has no bsn or kvk nummer assigned" }
        }
        throw UserTypeUnsupportedException("User type not supported")
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}