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
}