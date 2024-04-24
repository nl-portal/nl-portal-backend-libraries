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
package nl.nlportal.form.startform.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import nl.nlportal.form.graphql.FormDefinition
import nl.nlportal.form.startform.service.StartFormService

class StartFormQuery(
    private val startFormService: StartFormService,
) : Query {
    @GraphQLDescription("find all form definitions for start forms")
    suspend fun allStartForms(): List<FormDefinition> {
        return startFormService.getAllFormsLinkedToStartForms()
            .map { FormDefinition(it.formDefinition) }
    }
}