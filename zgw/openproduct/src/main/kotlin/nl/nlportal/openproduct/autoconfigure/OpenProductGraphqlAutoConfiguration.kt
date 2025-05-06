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
import nl.nlportal.openproduct.graphql.ProductTypeQuery
import nl.nlportal.openproduct.graphql.ThemaQuery
import nl.nlportal.openproduct.service.OpenProductService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

@ConditionalOnProperty(prefix = "nl-portal.config.openproduct", name = ["enabled"], havingValue = "true")
class OpenProductGraphqlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ThemaQuery::class)
    fun themaQuery(openProductService: OpenProductService): Query {
        return ThemaQuery(
            openProductService = openProductService,
        )
    }

    @Bean
    @ConditionalOnMissingBean(ProductTypeQuery::class)
    fun productTypeQuery(openProductService: OpenProductService): Query {
        return ProductTypeQuery(
            openProductService = openProductService,
        )
    }
}