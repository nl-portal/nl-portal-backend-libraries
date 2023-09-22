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
package nl.nlportal.klant.contactmomenten.autoconfiguration

import com.ritense.portal.klant.client.OpenKlantClient
import nl.nlportal.klant.contactmomenten.client.KlantContactMomentenClient
import nl.nlportal.klant.contactmomenten.graphql.ContactMomentQuery
import nl.nlportal.klant.contactmomenten.service.KlantContactMomentenService
import nl.nlportal.klant.generiek.client.OpenKlantClientConfig
import nl.nlportal.klant.generiek.client.OpenKlantClientProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenKlantClientConfig::class)
class KlantContactMomentenAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(KlantContactMomentenService::class)
    fun klantContactMomentenService(
        klantContactMomentenClient: KlantContactMomentenClient,
        klantClient: OpenKlantClient
    ): KlantContactMomentenService {
        return nl.nlportal.klant.contactmomenten.service.impl.KlantContactMomentenServiceImpl(
            klantContactMomentenClient,
            klantClient
        )
    }

    @Bean
    @ConditionalOnMissingBean(OpenKlantClientProvider::class)
    fun openKlantContactMomentenClient(
        openKlantClientProvider: OpenKlantClientProvider
    ): KlantContactMomentenClient {
        return KlantContactMomentenClient(openKlantClientProvider)
    }

    @Bean
    fun contactMomentenQuery(
        klantContactMomentenService: KlantContactMomentenService
    ): ContactMomentQuery {
        return ContactMomentQuery(klantContactMomentenService)
    }
}