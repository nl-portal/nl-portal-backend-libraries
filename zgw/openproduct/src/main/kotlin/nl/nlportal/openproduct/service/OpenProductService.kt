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
package nl.nlportal.openproduct.service

import nl.nlportal.openproduct.client.OpenProductClient
import nl.nlportal.openproduct.client.OpenProductTypeClient
import nl.nlportal.openproduct.client.domain.OpenProductThema
import nl.nlportal.openproduct.client.path.Themas
import java.util.UUID

class OpenProductService(
    private val openProductClient: OpenProductClient,
    private val openProductTypeClient: OpenProductTypeClient,
) {
    suspend fun getThemas(): List<OpenProductThema> {
        return openProductClient.path<Themas>().get()
    }

    suspend fun getThema(themaId: UUID): OpenProductThema? {
        return openProductClient.path<Themas>().get(themaId)
    }
}