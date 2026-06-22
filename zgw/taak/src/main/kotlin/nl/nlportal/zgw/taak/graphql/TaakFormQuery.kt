/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import nl.nlportal.form.graphql.FormDefinition
import nl.nlportal.form.service.ObjectsApiFormDefinitionService
import nl.nlportal.zgw.taak.domain.TaakFormulierV2
import nl.nlportal.zgw.taak.service.TaakService
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Controller
class TaakFormQuery(
    private val taakService: TaakService,
    private val objectsApiFormDefinitionService: ObjectsApiFormDefinitionService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @QueryMapping
    suspend fun getFormDefinitionByTaskId(
        @Argument taskId: UUID,
        authentication: CommonGroundAuthentication,
    ): FormDefinition? {
        val task = taakService.getTaakByIdV2(taskId, authentication)

        val formulier = task.portaalformulier?.formulier ?: return null
        val formDefUuid = extractUuid(formulier) ?: return null

        return try {
            objectsApiFormDefinitionService.getObjectsApiFormDefinitionById(formDefUuid)
        } catch (ex: RuntimeException) {
            logger.warn("Security: failed to fetch form definition for task {}: {}", taskId, ex.message)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not retrieve form definition")
        }
    }

    private fun extractUuid(formulier: TaakFormulierV2): UUID? =
        when (formulier.soort) {
            "id" -> runCatching { UUID.fromString(formulier.value) }.getOrNull()
            "url" ->
                Regex("/objects/([0-9a-fA-F-]{36})/?$")
                    .find(formulier.value)
                    ?.groupValues
                    ?.get(1)
                    ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            else -> {
                logger.warn("Unknown formulier.soort: {}", formulier.soort)
                null
            }
        }
}
