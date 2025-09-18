/*
 * Copyright 2025 Ritense BV, the Netherlands.
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
package nl.nlportal.payment.direct.graphql

import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.payment.direct.domain.DirectPaymentStatus
import nl.nlportal.payment.direct.service.DirectPaymentService

class DirectPaymentQuery(
    private val directPaymentService: DirectPaymentService,
) : Query {
    suspend fun getDirectPaymentStatus(
        identifier: String,
        hostedCheckoutId: String,
    ): DirectPaymentStatus =
        directPaymentService.getDirectPaymentStatus(
            identifier = identifier,
            hostedCheckoutId = hostedCheckoutId,
        )
}