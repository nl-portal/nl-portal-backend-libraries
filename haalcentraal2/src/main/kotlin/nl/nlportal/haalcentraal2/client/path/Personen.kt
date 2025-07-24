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
package nl.nlportal.haalcentraal2.client.path

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.haalcentraal2.client.HaalCentraal2BrpClient
import nl.nlportal.haalcentraal2.domain.brp.BrpApiRequest
import nl.nlportal.haalcentraal2.domain.brp.BrpApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.reactive.function.client.awaitBody
import kotlin.collections.isNullOrEmpty

class Personen(
    val client: HaalCentraal2BrpClient,
) : HaalCentraal2Path() {
    suspend fun post(
        brpApiRequest: BrpApiRequest,
        authentication: Authentication,
    ): BrpApiResponse =
        client.webClient
            .post()
            .uri("/personen")
            .headers {
                if (it[HttpHeaders.AUTHORIZATION].isNullOrEmpty()) {
                    it.setBearerAuth((authentication as CommonGroundAuthentication).token.tokenValue)
                }
            }.contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(brpApiRequest)
            .retrieve()
            .awaitBody<BrpApiResponse>()
}