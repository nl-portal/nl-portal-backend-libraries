/*
 * Copyright 2024-2025 Ritense BV, the Netherlands.
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
package nl.nlportal.openproduct.autoconfigure

import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.openproduct.graphql.OpenProductActieQuery
import nl.nlportal.openproduct.graphql.OpenProductContactQuery
import nl.nlportal.openproduct.graphql.OpenProductLinkQuery
import nl.nlportal.openproduct.graphql.OpenProductLocatieQuery
import nl.nlportal.openproduct.graphql.OpenProductOrganisatieQuery
import nl.nlportal.openproduct.graphql.OpenProductPrijsQuery
import nl.nlportal.openproduct.graphql.OpenProductQuery
import nl.nlportal.openproduct.graphql.OpenProductThemaQuery
import nl.nlportal.openproduct.graphql.OpenProductTypeQuery
import nl.nlportal.openproduct.service.OpenProductService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

@ConditionalOnProperty(prefix = "nl-portal.config.openproduct", name = ["enabled"], havingValue = "true")
class OpenProductGraphqlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(OpenProductThemaQuery::class)
    fun openProductthemaQuery(openProductService: OpenProductService): Query {
        return OpenProductThemaQuery(
            openProductService = openProductService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductTypeQuery::class)
    fun openProductTypeQuery(openProductService: OpenProductService): Query {
        return OpenProductTypeQuery(
            openProductService = openProductService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductQuery::class)
    fun openProductQuery(openProductService: OpenProductService): Query {
        return OpenProductQuery(
            openProductService = openProductService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductActieQuery::class)
    fun openProductActieQuery(openProductService: OpenProductService): Query {
        return OpenProductActieQuery(openProductService = openProductService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductContactQuery::class)
    fun openProductContactQuery(openProductService: OpenProductService): Query {
        return OpenProductContactQuery(openProductService = openProductService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductLocatieQuery::class)
    fun openProductLocatieQuery(openProductService: OpenProductService): Query {
        return OpenProductLocatieQuery(openProductService = openProductService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductLinkQuery::class)
    fun openProductLinkQuery(openProductService: OpenProductService): Query {
        return OpenProductLinkQuery(openProductService = openProductService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductOrganisatieQuery::class)
    fun openProductOrganisatieQuery(openProductService: OpenProductService): Query {
        return OpenProductOrganisatieQuery(openProductService = openProductService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenProductPrijsQuery::class)
    fun openProductPrijsQuery(openProductService: OpenProductService): Query {
        return OpenProductPrijsQuery(openProductService = openProductService)
    }
}