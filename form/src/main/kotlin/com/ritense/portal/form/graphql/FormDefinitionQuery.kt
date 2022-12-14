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
package com.ritense.portal.form.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.ritense.portal.form.service.FormIoFormDefinitionService

class FormDefinitionQuery(private val formIoFormDefinitionService: FormIoFormDefinitionService) : Query {

    @GraphQLDescription("find all form definitions from repository")
    fun allFormDefinitions(): List<FormDefinition> {
        return formIoFormDefinitionService.findAllFormDefinitions()
            .map { FormDefinition(it.name, it.formDefinition) }
    }

    @GraphQLDescription("find single form definition from repository")
    fun getFormDefinition(
        @GraphQLDescription("The form definition name") name: String
    ): FormDefinition? {
        val formIoFormDefinition = formIoFormDefinitionService.findFormIoFormDefinition(name) ?: return null
        return FormDefinition(formIoFormDefinition.name, formIoFormDefinition.formDefinition)
    }
}