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
package nl.nlportal.core.ssl

import io.netty.handler.ssl.SslContext

interface ClientSslContextResolver {

    /**
     * This will resolve the SSL context which can be used for client authentication
     *
     * The private key can either be encrypted by a password, or unencrypted.
     * Please note: the supported encryption cyphers depends on the JRE used.
     */
    fun resolve(keyData: ClientKey? = null, trustedCertificate: String? = null): SslContext
}