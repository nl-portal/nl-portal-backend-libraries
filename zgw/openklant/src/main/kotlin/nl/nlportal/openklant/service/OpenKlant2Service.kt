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
package nl.nlportal.openklant.service

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.openklant.client.OpenKlant2Client
import nl.nlportal.openklant.domain.Partij

class OpenKlant2Service(
    private val enabled: Boolean = false,
    private val openKlant2Client: OpenKlant2Client
) {

    suspend fun getPartij(authentication: CommonGroundAuthentication): Partij? {
        if (!enabled) return null

        return openKlant2Client.getPartij(authentication)
    }
}