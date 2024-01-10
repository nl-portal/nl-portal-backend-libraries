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
import io.netty.handler.ssl.SslContextBuilder
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class StringClientSslContextResolver : ClientSslContextResolver {
    private val certificateFactory = CertificateFactory.getInstance("X509")

    override fun resolve(
        keyData: ClientKey?,
        trustedCertificate: String?,
    ): SslContext {
        val contextBuilder = SslContextBuilder.forClient()
        keyData?.let { data ->
            data.certChain.byteInputStream().use { certChainInputStream ->
                data.key.byteInputStream().use { keyInputStream ->
                    if (keyData.keyPassword.isNullOrBlank()) {
                        contextBuilder.keyManager(certChainInputStream, keyInputStream)
                    } else {
                        contextBuilder.keyManager(certChainInputStream, certChainInputStream, data.keyPassword)
                    }
                }
            }
        }

        trustedCertificate?.let {
            it.byteInputStream().use { stream ->
                contextBuilder.trustManager(certificateFactory.generateCertificate(stream) as X509Certificate)
            }
        }

        return contextBuilder.build()
    }
}