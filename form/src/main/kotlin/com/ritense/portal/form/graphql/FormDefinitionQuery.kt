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
import com.ritense.valtimo.portal.form.service.ObjectsApiFormDefinitionService

class FormDefinitionQuery(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val objectenApiFormDefinitionService: ObjectsApiFormDefinitionService
) : Query {

    @GraphQLDescription("find all form definitions from repository")
    @Deprecated("Deprecated")
    fun allFormDefinitions(): List<FormDefinition> {
        return formIoFormDefinitionService.findAllFormDefinitions()
            .map { FormDefinition(it.formDefinition) }
    }

    @GraphQLDescription("find single form definition from repository")
    @Deprecated("Replaced by getFormDefinitionById and getFormDefinitionByObjectenApiUrl")
    fun getFormDefinition(
        @GraphQLDescription("The form definition name") name: String
    ): FormDefinition? {
        val formIoFormDefinition = formIoFormDefinitionService.findFormIoFormDefinition(name) ?: return null
        return FormDefinition(formIoFormDefinition.formDefinition)
    }

    fun getFormDefinitionById(
        @GraphQLDescription("The form definition id") id: String
    ): FormDefinition? {
        val formIoFormDefinition = formIoFormDefinitionService.findFormIoFormDefinition(id) ?: return null
        return FormDefinition(formIoFormDefinition.formDefinition)
    }

    suspend fun getFormDefinitionByObjectenApiUrl(
        @GraphQLDescription("The form definition url") url: String
    ): FormDefinition? {
        val objectenApiFormDefinition = objectenApiFormDefinitionService.findObjectsApiFormDefinition(url) ?: return null
        return FormDefinition(objectenApiFormDefinition.formDefinition)
    }
}