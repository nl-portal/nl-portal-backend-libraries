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
package nl.nlportal.besluiten

import java.nio.charset.StandardCharsets
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils

@Configuration
class GraphQLTestConfiguration {
    @Value("classpath:graphql/getBesluiten.graphql")
    private lateinit var getBesluiten: Resource

    @Value("classpath:graphql/getBesluit.graphql")
    private lateinit var getBesluit: Resource

    @Value("classpath:graphql/getBesluitAuditTrails.graphql")
    private lateinit var getBesluitAuditTrails: Resource

    @Value("classpath:graphql/getBesluitAuditTrail.graphql")
    private lateinit var getBesluitAuditTrail: Resource

    @Value("classpath:graphql/getBesluitDocumenten.graphql")
    private lateinit var getBesluitDocumenten: Resource

    @Value("classpath:graphql/getBesluitDocument.graphql")
    private lateinit var getBesluitDocument: Resource

    @Bean
    fun graphqlGetBesluiten(): String {
        return StreamUtils.copyToString(
            getBesluiten.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetBesluit(): String {
        return StreamUtils.copyToString(
            getBesluit.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetBesluitAuditTrails(): String {
        return StreamUtils.copyToString(
            getBesluitAuditTrails.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetBesluitAuditTrail(): String {
        return StreamUtils.copyToString(
            getBesluitAuditTrail.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetBesluitDocumenten(): String {
        return StreamUtils.copyToString(
            getBesluitDocumenten.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun graphqlGetBesluitDocument(): String {
        return StreamUtils.copyToString(
            getBesluitDocument.inputStream,
            StandardCharsets.UTF_8,
        )
    }
}