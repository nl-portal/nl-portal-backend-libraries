/*
 * Copyright 2023 Ritense BV, the Netherlands.
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
package nl.nlportal.klant.generiek.client

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import io.netty.handler.logging.LogLevel
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

class OpenKlantClientProvider(
    private val openKlantClientConfig: OpenKlantClientConfig,
    private val idTokenGenerator: IdTokenGenerator,
) {
    fun webClient(authentication: CommonGroundAuthentication): WebClient {
        val token =
            idTokenGenerator.generateToken(
                openKlantClientConfig.secret,
                openKlantClientConfig.clientId,
                authentication.getUserId(),
                authentication.getUserRepresentation(),
            )

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(
                        "reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG,
                        AdvancedByteBufFormat.TEXTUAL,
                    ),
                ),
            )
            .baseUrl(openKlantClientConfig.url)
            .defaultHeader("Accept-Crs", "EPSG:4326")
            .defaultHeader("Content-Crs", "EPSG:4326")
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}