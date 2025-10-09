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
package nl.nlportal.form.graphql

import java.util.UUID.fromString
import nl.nlportal.form.service.FormIoFormDefinitionService
import nl.nlportal.form.service.ObjectsApiFormDefinitionService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class FormDefinitionQuery(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val objectenApiFormDefinitionService: ObjectsApiFormDefinitionService,
) {
    @QueryMapping
    fun getFormDefinitionByName(
        @Argument name: String,
    ): FormDefinition? {
        val formIoFormDefinition = formIoFormDefinitionService.findFormIoFormDefinitionByName(name) ?: return null
        return FormDefinition(formIoFormDefinition.formDefinition)
    }

    @QueryMapping
    suspend fun getFormDefinitionByObjectenApiUrl(
        @Argument url: String,
    ): FormDefinition? {
        val objectenApiFormDefinition = objectenApiFormDefinitionService.findObjectsApiFormDefinitionByUrl(url) ?: return null
        return FormDefinition(objectenApiFormDefinition.formDefinition)
    }

    @Deprecated(
        message = "Replaced by getFormDefinitionByName and getFormDefinitionByObjectenApiUrl",
        replaceWith = ReplaceWith("getFormDefinitionByName or getFormDefinitionByObjectenApiUrl"),
    )
    @QueryMapping
    suspend fun getFormDefinitionById(
        @Argument id: String,
    ): FormDefinition? {
        // for backwards compatibility
        // if the requested id is a UUID, we assume it's an Objecten API id
        // when the nl-portal-frontend-libraries has been migrated, this method will be removed
        if (requestedIdIsUuid(id)) {
            val formIoFormDefinition = objectenApiFormDefinitionService.findObjectsApiFormDefinitionById(id) ?: return null
            return FormDefinition(formIoFormDefinition.formDefinition)
        } else {
            val formIoFormDefinition = formIoFormDefinitionService.findFormIoFormDefinitionByName(id) ?: return null
            return FormDefinition(formIoFormDefinition.formDefinition)
        }
    }

    private fun requestedIdIsUuid(id: String): Boolean {
        try {
            fromString(id)
            return true
        } catch (e: IllegalArgumentException) {
            return false
        }
    }
}