/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
package nl.nlportal.berichten.graphql

import java.util.UUID
import nl.nlportal.berichten.domain.Bericht
import nl.nlportal.berichten.service.BerichtenService
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class BerichtenQuery(
    private val berichtenService: BerichtenService,
) {
    @QueryMapping
    suspend fun getBericht(
        authentication: CommonGroundAuthentication,
        @Argument id: UUID,
    ): Bericht? =
        berichtenService.getBericht(
            authentication = authentication,
            id = id,
        )

    @QueryMapping
    suspend fun getBerichten(
        authentication: CommonGroundAuthentication,
        @Argument pageNumber: Int? = 1,
        @Argument pageSize: Int? = 20,
        @Argument onderwerp: String? = null,
    ): BerichtenPage =
        berichtenService.getBerichtenPage(
            authentication = authentication,
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            onderwerp = onderwerp,
        )

    @QueryMapping
    suspend fun getUnopenedBerichtenCount(authentication: CommonGroundAuthentication): Int =
        berichtenService
            .getUnopenedBerichtenCount(
                authentication = authentication,
            )
}