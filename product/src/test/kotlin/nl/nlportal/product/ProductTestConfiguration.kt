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
package nl.nlportal.product

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

@Configuration
class ProductTestConfiguration {
    @Value("classpath:product/graphql/getProduct.graphql")
    private lateinit var getProduct: Resource

    @Value("classpath:product/graphql/getProducten.graphql")
    private lateinit var getProducten: Resource

    @Value("classpath:product/graphql/updateProductVerbruiksObject.graphql")
    private lateinit var updateProductVerbruiksObject: Resource

    @Value("classpath:product/graphql/getProductZaken.graphql")
    private lateinit var getProductZaken: Resource

    @Value("classpath:product/graphql/getProductTaken.graphql")
    private lateinit var getProductTaken: Resource

    @Value("classpath:product/graphql/getProductZakenNotFound.graphql")
    private lateinit var getProductZakenNotFound: Resource

    @Value("classpath:product/graphql/getProductVerbruiksObjecten.graphql")
    private lateinit var getProductVerbruiksObjecten: Resource

    @Value("classpath:product/graphql/getProductType.graphql")
    private lateinit var getProductType: Resource

    @Value("classpath:product/graphql/getProductTypes.graphql")
    private lateinit var getProductTypes: Resource

    @Value("classpath:product/graphql/prefill.graphql")
    private lateinit var prefill: Resource

    @Value("classpath:product/graphql/getProductDecision.graphql")
    private lateinit var getProductDecision: Resource

    @Bean
    fun graphqlGetProducten(): String {
        return StreamUtils.copyToString(
            getProducten.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProduct(): String {
        return StreamUtils.copyToString(
            getProduct.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlUpdateProductVerbruiksObject(): String {
        return StreamUtils.copyToString(
            updateProductVerbruiksObject.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductZaken(): String {
        return StreamUtils.copyToString(
            getProductZaken.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductTaken(): String {
        return StreamUtils.copyToString(
            getProductTaken.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductZakenNotFound(): String {
        return StreamUtils.copyToString(
            getProductZakenNotFound.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductVerbruiksObjecten(): String {
        return StreamUtils.copyToString(
            getProductVerbruiksObjecten.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductType(): String {
        return StreamUtils.copyToString(
            getProductType.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductTypes(): String {
        return StreamUtils.copyToString(
            getProductTypes.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlPrefill(): String {
        return StreamUtils.copyToString(
            prefill.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetProductDecision(): String {
        return StreamUtils.copyToString(
            getProductDecision.inputStream,
            StandardCharsets.UTF_8,
        )
    }
}