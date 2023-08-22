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

import com.ritense.portal.documentenapi.client.ClamAVVirusScanConfig
import com.ritense.portal.documentenapi.client.DocumentenApiClient
import com.ritense.portal.documentenapi.client.DocumentenApiConfig
import com.ritense.portal.documentenapi.client.DocumentenApiVirusScanConfig
import com.ritense.portal.documentenapi.graphql.DocumentContentQuery
import com.ritense.portal.documentenapi.security.config.DocumentContentResourceHttpSecurityConfigurer
import com.ritense.portal.documentenapi.service.DocumentenApiService
import com.ritense.portal.documentenapi.service.VirusScanService
import com.ritense.portal.documentenapi.service.impl.ClamAVService
import com.ritense.portal.documentenapi.web.rest.DocumentContentResource
import com.ritense.portal.idtokenauthentication.service.IdTokenGenerator
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xyz.capybara.clamav.ClamavClient

@Configuration
@EnableConfigurationProperties(DocumentenApiConfig::class, DocumentenApiVirusScanConfig::class, ClamAVVirusScanConfig::class)
class DocumentenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DocumentenApiService::class)
    fun documentenApiService(
        documentenApiClient: DocumentenApiClient,
        documentenApiConfig: DocumentenApiConfig
    ): DocumentenApiService {
        return DocumentenApiService(documentenApiClient, documentenApiConfig)
    }

    @Bean
    @ConditionalOnMissingBean(VirusScanService::class)
    fun virusScanService(
        clamAVClient: ClamavClient
    ): VirusScanService {
        return ClamAVService(clamAVClient)
    }

    @Bean
    fun clamAVClient(
        clamAVVirusScanConfig: ClamAVVirusScanConfig
    ): ClamavClient {
        logger.info("ClamAV virusscan is loaded with host: {} and port: {}", clamAVVirusScanConfig.hostName, clamAVVirusScanConfig.port)
        return ClamavClient(
            clamAVVirusScanConfig.hostName,
            clamAVVirusScanConfig.port
        )
    }
    @Bean
    fun documentenApiConfig(): DocumentenApiConfig {
        return DocumentenApiConfig()
    }

    @Bean
    fun documentenApiVirusScanConfig(): DocumentenApiVirusScanConfig {
        return DocumentenApiVirusScanConfig()
    }

    @Bean
    fun documentenApiClient(
        documentenApiConfig: DocumentenApiConfig,
        idTokenGenerator: IdTokenGenerator
    ): DocumentenApiClient {
        return DocumentenApiClient(documentenApiConfig, idTokenGenerator)
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
        virusScanService: VirusScanService,
        documentenApiVirusScanConfig: DocumentenApiVirusScanConfig
    ): DocumentContentResource {
        return DocumentContentResource(documentenApiClient, documentenApiService, virusScanService, documentenApiVirusScanConfig)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}