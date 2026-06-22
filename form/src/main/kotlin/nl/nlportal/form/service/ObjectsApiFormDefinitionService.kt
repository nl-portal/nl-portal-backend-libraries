/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package nl.nlportal.form.service

import nl.nlportal.form.autoconfigure.FormConfig
import nl.nlportal.form.domain.ObjectsApiFormIoFormDefinition
import nl.nlportal.form.graphql.FormDefinition
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import org.slf4j.LoggerFactory
import java.util.UUID

class ObjectsApiFormDefinitionService(
    private val objectenApiService: ObjectenApiService,
    private val formConfig: FormConfig,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun getObjectsApiFormDefinitionById(objectId: UUID): FormDefinition? {
        val obj =
            objectenApiService.getObjectById<ObjectsApiFormIoFormDefinition>(objectId.toString())
                ?: return null

        val expected = formConfig.properties.formDefinitionObjectTypeUrl
        if (expected == null) {
            logger.debug("formDefinitionObjectTypeUrl not configured — skipping type validation")
        } else if (obj.type != expected) {
            throw RuntimeException(
                "Object type mismatch for form definition fetch. " +
                    "Expected: $expected, Actual: ${obj.type}, ObjectId: $objectId",
            )
        }

        return FormDefinition(obj.record.data.formDefinition)
    }
}