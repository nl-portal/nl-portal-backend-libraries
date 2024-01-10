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
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class ResourceClientSslContextResolver(
    private val resourceLoader: ResourceLoader = DefaultResourceLoader(),
) : ClientSslContextResolver {
    private val certificateFactory = CertificateFactory.getInstance("X509")

    override fun resolve(
        keyData: ClientKey?,
        trustedCertificate: String?,
    ): SslContext {
        val contextBuilder = SslContextBuilder.forClient()

        keyData?.let { data ->
            resourceLoader.getResource(data.certChain).inputStream.use { certChainInputStream ->
                resourceLoader.getResource(data.key).inputStream.use { keyInputStream ->
                    if (keyData.keyPassword.isNullOrBlank()) {
                        contextBuilder.keyManager(certChainInputStream, keyInputStream)
                    } else {
                        contextBuilder.keyManager(certChainInputStream, keyInputStream, data.keyPassword)
                    }
                }
            }
        }

        trustedCertificate?.let {
            resourceLoader.getResource(it).inputStream.use { stream ->
                contextBuilder.trustManager(certificateFactory.generateCertificate(stream) as X509Certificate)
            }
        }

        return contextBuilder.build()
    }
}