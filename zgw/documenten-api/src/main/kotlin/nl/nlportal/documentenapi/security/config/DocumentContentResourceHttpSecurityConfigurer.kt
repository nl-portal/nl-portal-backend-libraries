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
package nl.nlportal.documentenapi.security.config

import nl.nlportal.core.security.config.HttpSecurityConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.web.server.ServerHttpSecurity

@Configuration
class DocumentContentResourceHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: ServerHttpSecurity) {
        http.authorizeExchange { authorize ->
            authorize.pathMatchers(GET, "/api/document/{documentId}/content").authenticated()
        }

        http.authorizeExchange { authorize ->
            authorize.pathMatchers(POST, "/api/document/content").authenticated()
        }
    }
}