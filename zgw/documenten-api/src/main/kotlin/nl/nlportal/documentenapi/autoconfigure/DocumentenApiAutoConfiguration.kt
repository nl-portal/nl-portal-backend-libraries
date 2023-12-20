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
package nl.nlportal.documentenapi.autoconfigure

import nl.nlportal.core.ssl.ClientSslContextResolver
import nl.nlportal.core.ssl.ResourceClientSslContextResolver
import nl.nlportal.documentenapi.client.DocumentApisConfig
import nl.nlportal.documentenapi.client.DocumentenApiClient
import nl.nlportal.documentenapi.graphql.DocumentContentQuery
import nl.nlportal.documentenapi.security.config.DocumentContentResourceHttpSecurityConfigurer
import nl.nlportal.documentenapi.service.DocumentenApiService
import nl.nlportal.documentenapi.service.VirusScanService
import nl.nlportal.documentenapi.web.rest.DocumentContentResource
import nl.nlportal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.io.ResourceLoader

@Configuration
@EnableConfigurationProperties(DocumentApisConfig::class)
@Import(ClamAVConfiguration::class)
class DocumentenApiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ClientSslContextResolver::class)
    fun clientSslContextResolver(resourceLoader: ResourceLoader): ClientSslContextResolver {
        return ResourceClientSslContextResolver(resourceLoader)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentenApiService::class)
    fun documentenApiService(
        documentenApiClient: DocumentenApiClient,
        documentApisConfig: DocumentApisConfig,
    ): DocumentenApiService {
        return DocumentenApiService(documentenApiClient, documentApisConfig)
    }

    @Bean
    fun documentenApiClient(
        documentApisConfig: DocumentApisConfig,
        idTokenGenerator: IdTokenGenerator,
        @Autowired(required = false) clientSslContextResolver: ClientSslContextResolver? = null,
    ): DocumentenApiClient {
        return DocumentenApiClient(documentApisConfig, idTokenGenerator, clientSslContextResolver)
    }

    @Bean
    fun documentContentQuery2(documentenApiService: DocumentenApiService): DocumentContentQuery {
        return DocumentContentQuery(documentenApiService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentContentResourceHttpSecurityConfigurer::class)
    fun documentContentResourceHttpSecurityConfigurer2(): DocumentContentResourceHttpSecurityConfigurer {
        return DocumentContentResourceHttpSecurityConfigurer()
    }

    @Bean
    @ConditionalOnMissingBean(DocumentContentResource::class)
    fun documentContentResource2(
        documentenApiClient: DocumentenApiClient,
        documentenApiService: DocumentenApiService,
        virusScanService: VirusScanService?,
        documentApisConfig: DocumentApisConfig,
    ): DocumentContentResource {
        return DocumentContentResource(documentenApiClient, documentenApiService, virusScanService, documentApisConfig)
    }
}