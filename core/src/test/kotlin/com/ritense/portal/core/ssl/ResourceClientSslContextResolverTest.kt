/*
 * Copyright 2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.core.ssl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ResourceClientSslContextResolverTest {

    val sslContextResolver = ResourceClientSslContextResolver()

    @Test
    fun `should resolve an unencrypted private key and certificate chain`() {
        val certResource = "classpath:/ssl/client-cert.pem"
        val keyResource = "classpath:/ssl/client-key.pem"

        val sslContextSpec = sslContextResolver.resolve(ClientKey(certResource, keyResource))

        assertThat(sslContextSpec).isNotNull
    }

    @Test
    fun `should resolve an encrypted private key and certificate chain`() {
        val certResource = "classpath:/ssl/client-cert.pem"
        val keyResource = "classpath:/ssl/client-key-pkcs8.pem"

        val sslContextSpec = sslContextResolver.resolve(ClientKey(certResource, keyResource, "test"))

        assertThat(sslContextSpec).isNotNull
    }
}