/*
 * Copyright 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.openklant.autoconfigure

import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import nl.nlportal.openklant.client.OpenKlant2VerificatieClient
import nl.nlportal.openklant.service.OpenKlant2Service
import nl.nlportal.openklant.service.OpenKlantVerificatieService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@EnableConfigurationProperties(
    OpenKlantModuleConfiguration::class,
)
class OpenKlantAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(OpenKlant2KlantinteractiesClient::class)
    fun openKlant2Client(openklantModuleConfiguration: OpenKlantModuleConfiguration): OpenKlant2KlantinteractiesClient = OpenKlant2KlantinteractiesClient(openKlantConfigurationProperties = openklantModuleConfiguration.properties)

    @Bean
    @ConditionalOnMissingBean(OpenKlant2Service::class)
    fun openKlant2Service(
        openklant2Client: OpenKlant2KlantinteractiesClient,
        openKlantModuleConfiguration: OpenKlantModuleConfiguration,
    ): OpenKlant2Service =
        OpenKlant2Service(
            openKlant2Client = openklant2Client,
            openKlantConfigurationProperties = openKlantModuleConfiguration.properties,
        )

    @Bean("openKlantVerificatieClient")
    fun openKlantVerificatieClient(
        openklantModuleConfiguration: OpenKlantModuleConfiguration,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
        webClientBuilder: WebClient.Builder,
    ): OpenKlant2VerificatieClient =
        OpenKlant2VerificatieClient(
            verificatieConfigurationProperties = openklantModuleConfiguration.properties.verificatie,
            clientSslContextResolver = clientSslContextResolver,
            webClientBuilder = webClientBuilder,
        )

    @Bean
    fun openKlantVerificatieService(
        openklantModuleConfiguration: OpenKlantModuleConfiguration,
        openKlant2VerificatieClient: OpenKlant2VerificatieClient,
        openKlant2Service: OpenKlant2Service,
    ) = OpenKlantVerificatieService(
        verificatieConfigurationProperties = openklantModuleConfiguration.properties.verificatie,
        verificatieClient = openKlant2VerificatieClient,
        openKlant2Service = openKlant2Service,
    )
}