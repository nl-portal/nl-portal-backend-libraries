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
package nl.nlportal.zgw.taak

import java.nio.charset.StandardCharsets
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils

@Configuration
class GraphQLTestConfiguration {
    @Value("classpath:graphql/getTakenQuery.graphql")
    private lateinit var getTakenFile: Resource

    @Value("classpath:graphql/getTaakByIdQuery.graphql")
    private lateinit var getTaakByIdFile: Resource

    @Value("classpath:graphql/getTaakByIdQueryBedrijf.graphql")
    private lateinit var getTaakByIdFileBedrijf: Resource

    @Value("classpath:graphql/getTakenQueryV2.graphql")
    private lateinit var getTakenFileV2: Resource

    @Value("classpath:graphql/getTaakByIdQueryV2.graphql")
    private lateinit var getTaakByIdFileV2: Resource

    @Value("classpath:graphql/getTaakByIdQueryV2Bedrijf.graphql")
    private lateinit var getTaakByIdFileV2Bedrijf: Resource

    @Value("classpath:graphql/updateTaakV2.graphql")
    private lateinit var updateTaakV2: Resource

    @Bean
    fun getTakenPayload(): String {
        return StreamUtils.copyToString(
            getTakenFile.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun getTaakByIdPayload(): String {
        return StreamUtils.copyToString(
            getTaakByIdFile.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun getTaakByIdPayloadBedrijf(): String {
        return StreamUtils.copyToString(
            getTaakByIdFileBedrijf.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun getTakenPayloadV2(): String {
        return StreamUtils.copyToString(
            getTakenFileV2.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun getTaakByIdPayloadV2(): String {
        return StreamUtils.copyToString(
            getTaakByIdFileV2.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun getTaakByIdPayloadV2Bedrijf(): String {
        return StreamUtils.copyToString(
            getTaakByIdFileV2Bedrijf.inputStream,
            StandardCharsets.UTF_8,
        )
    }

    @Bean
    fun updateTaakPayloadV2(): String {
        return StreamUtils.copyToString(
            updateTaakV2.inputStream,
            StandardCharsets.UTF_8,
        )
    }
}