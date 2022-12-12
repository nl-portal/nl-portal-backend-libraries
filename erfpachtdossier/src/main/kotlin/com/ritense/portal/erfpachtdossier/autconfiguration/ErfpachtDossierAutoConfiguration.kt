/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.erfpachtdossier.autconfiguration

import com.ritense.portal.erfpachtdossier.client.ErfpachtDossierClient
import com.ritense.portal.erfpachtdossier.client.ErfpachtDossierClientConfig
import com.ritense.portal.erfpachtdossier.graphql.ErfpachtdossierQuery
import com.ritense.portal.erfpachtdossier.service.impl.ErfpachtDossierService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ErfpachtDossierClientConfig::class)
class ErfpachtDossierAutoConfiguration {

//    @Bean
//    fun erfpachtDossierClientConfig(): ErfpachtDossierClientConfig {
//        return ErfpachtDossierClientConfig()
//    }

    @Bean
    @ConditionalOnMissingBean(ErfpachtDossierClient::class)
    fun erfpachtDossierClient(
        erfpachtDossierClientConfig: ErfpachtDossierClientConfig
    ): ErfpachtDossierClient {
        return ErfpachtDossierClient(erfpachtDossierClientConfig)
    }

    @Bean
    @ConditionalOnMissingBean(ErfpachtDossierService::class)
    fun erfpachtDossierService(
        erfachtdossierClient: ErfpachtDossierClient
    ): ErfpachtDossierService {
        return ErfpachtDossierService(erfachtdossierClient)
    }

    @Bean
    fun erfpachtdossierQuery(
        erfpachtDossierService: ErfpachtDossierService
    ): ErfpachtdossierQuery {
        return ErfpachtdossierQuery(erfpachtDossierService)
    }
}