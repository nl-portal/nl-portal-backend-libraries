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
package nl.nlportal.zgw.taak.graphql

import java.util.UUID
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.zgw.taak.domain.TaakStatus
import nl.nlportal.zgw.taak.domain.TaakV2
import nl.nlportal.zgw.taak.service.TaakService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class TaakQueryV2(
    private val taskService: TaakService,
) {
    @QueryMapping
    suspend fun getTakenV2(
        authentication: CommonGroundAuthentication,
        @Argument pageNumber: Int? = 1,
        @Argument pageSize: Int? = 20,
        @Argument zaakUUID: UUID? = null,
        @Argument status: TaakStatus? = null,
        @Argument title: String? = null,
    ): TaakPageV2 {
        return taskService.getTaken(
            pageNumber = pageNumber ?: 1,
            pageSize = pageSize ?: 20,
            authentication = authentication,
            zaakUUID = zaakUUID,
            status = status,
            title = title,
        )
    }

    @QueryMapping
    suspend fun getTaakByIdV2(
        @Argument id: UUID,
        authentication: CommonGroundAuthentication,
    ): TaakV2? {
        return taskService.getTaakByIdV2(
            id = id,
            authentication = authentication
        )
    }
}
