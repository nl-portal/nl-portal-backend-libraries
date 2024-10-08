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

import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.openklant.client.OpenKlant2KlantinteractiesClient
import nl.nlportal.openklant.graphql.DigitaleAdresMutation
import nl.nlportal.openklant.graphql.DigitaleAdresQuery
import nl.nlportal.openklant.graphql.PartijMutation
import nl.nlportal.openklant.graphql.PartijQuery
import nl.nlportal.openklant.service.OpenKlant2Service
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@EnableConfigurationProperties(
    OpenKlantModuleConfiguration::class,
)
class OpenKlantAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(OpenKlant2KlantinteractiesClient::class)
    fun openKlant2Client(openklantModuleConfiguration: OpenKlantModuleConfiguration): OpenKlant2KlantinteractiesClient {
        return OpenKlant2KlantinteractiesClient(openKlantConfigurationProperties = openklantModuleConfiguration.properties)
    }

    @Bean
    @ConditionalOnMissingBean(OpenKlant2Service::class)
    fun openKlant2Service(openklant2Client: OpenKlant2KlantinteractiesClient): OpenKlant2Service {
        return OpenKlant2Service(openklant2Client)
    }

    @Bean
    @ConditionalOnMissingBean(
        PartijQuery::class,
    )
    @ConditionalOnProperty(prefix = "nl-portal.config.openklant", name = ["enabled"], havingValue = "true")
    fun partijQuery(openKlant2Service: OpenKlant2Service): Query {
        return PartijQuery(openKlant2Service)
    }

    @Bean
    @ConditionalOnMissingBean(
        PartijMutation::class,
    )
    @ConditionalOnProperty(prefix = "nl-portal.config.openklant", name = ["enabled"], havingValue = "true")
    fun partijMutation(openKlant2Service: OpenKlant2Service): Mutation {
        return PartijMutation(openKlant2Service)
    }

    @Bean
    @ConditionalOnMissingBean(
        DigitaleAdresQuery::class,
    )
    @ConditionalOnProperty(prefix = "nl-portal.config.openklant", name = ["enabled"], havingValue = "true")
    fun digitaleAdresQuery(openKlant2Service: OpenKlant2Service): Query {
        return DigitaleAdresQuery(openKlant2Service)
    }

    @Bean
    @ConditionalOnMissingBean(
        DigitaleAdresMutation::class,
    )
    @ConditionalOnProperty(prefix = "nl-portal.config.openklant", name = ["enabled"], havingValue = "true")
    fun digitaleAdresMutation(openKlant2Service: OpenKlant2Service): Mutation {
        return DigitaleAdresMutation(openKlant2Service)
    }
}