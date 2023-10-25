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
package com.ritense.portal.documentenapi.autoconfigure

import com.ritense.portal.documentenapi.client.DocumentApisConfig
import com.ritense.portal.documentenapi.client.DocumentenApiClient
import com.ritense.portal.documentenapi.graphql.DocumentContentQuery
import com.ritense.portal.documentenapi.security.config.DocumentContentResourceHttpSecurityConfigurer
import com.ritense.portal.documentenapi.service.DocumentenApiService
import com.ritense.portal.documentenapi.service.VirusScanService
import com.ritense.portal.documentenapi.web.rest.DocumentContentResource
import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(DocumentApisConfig::class)
class DocumentenApiAutoConfiguration {

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
    ): DocumentenApiClient {
        return DocumentenApiClient(documentApisConfig, idTokenGenerator)
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
    ): DocumentContentResource {
        return DocumentContentResource(documentenApiClient, documentenApiService, virusScanService)
    }
}