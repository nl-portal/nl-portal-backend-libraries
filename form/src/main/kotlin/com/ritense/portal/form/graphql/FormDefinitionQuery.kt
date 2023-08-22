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
package com.ritense.portal.form.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.form.service.FormIoFormDefinitionService
import com.ritense.portal.form.service.ObjectsApiFormDefinitionService
import java.util.UUID.fromString

class FormDefinitionQuery(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val objectenApiFormDefinitionService: ObjectsApiFormDefinitionService
) : Query {

    @GraphQLDescription("find all form definitions from repository")
    @Deprecated("This method is not used by the NL Portal frontend and is not being replaced.")
    fun allFormDefinitions(): List<FormDefinition> {
        return formIoFormDefinitionService.findAllFormDefinitions()
            .map { FormDefinition(it.formDefinition) }
    }

    @GraphQLDescription("find single form definition from repository")
    fun getFormDefinitionByName(
        @GraphQLDescription("The form definition name") name: String
    ): FormDefinition? {
        val formIoFormDefinition = formIoFormDefinitionService.findFormIoFormDefinitionByName(name) ?: return null
        return FormDefinition(formIoFormDefinition.formDefinition)
    }

    @GraphQLDescription("find single form definition from the Objecten API")
    suspend fun getFormDefinitionByObjectenApiUrl(
        @GraphQLDescription("The form definition url") url: String
    ): FormDefinition? {
        val objectenApiFormDefinition = objectenApiFormDefinitionService.findObjectsApiFormDefinitionByUrl(url) ?: return null
        return FormDefinition(objectenApiFormDefinition.formDefinition)
    }

    @Deprecated(
        message = "Replaced by getFormDefinitionByName and getFormDefinitionByObjectenApiUrl",
        replaceWith = ReplaceWith("getFormDefinitionByName or getFormDefinitionByObjectenApiUrl")
    )
    @GraphQLDescription("find single form definition from repository or Objecten API")
    suspend fun getFormDefinitionById(
        @GraphQLDescription("The form definition id") id: String
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