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
package nl.nlportal.zakenapi.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import nl.nlportal.catalogiapi.client.CatalogiApiConfig
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.zakenapi.client.ZakenApiClient
import nl.nlportal.zakenapi.client.ZakenApiConfig
import nl.nlportal.zakenapi.graphql.ZaakQuery
import nl.nlportal.zakenapi.service.ZakenApiService
import nl.nlportal.zgw.objectenapi.client.ObjectsApiClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(ZakenApiConfig::class)
class ZakenApiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ZakenApiService::class)
    fun zakenApiService(
        zakenApiClient: ZakenApiClient,
        documentenApiService: DocumentenApiService,
        objectsApiClient: ObjectsApiClient,
        objectMapper: ObjectMapper,
    ): ZakenApiService {
        return ZakenApiService(
            zakenApiClient,
            documentenApiService,
            objectsApiClient,
        )
    }

    @Bean
    fun zakenApiConfig(): ZakenApiConfig {
        return ZakenApiConfig()
    }

    @Bean
    fun zakenApiClient(
        zakenApiConfig: ZakenApiConfig,
        catalogiApiConfig: CatalogiApiConfig,
        webClientBuilder: WebClient.Builder,
    ): ZakenApiClient {
        return ZakenApiClient(zakenApiConfig, catalogiApiConfig, webClientBuilder)
    }

    @Bean
    @ConditionalOnMissingBean(ZaakQuery::class)
    fun zaakListQuery2(zaakService: ZakenApiService): ZaakQuery {
        return ZaakQuery(zaakService)
    }
}