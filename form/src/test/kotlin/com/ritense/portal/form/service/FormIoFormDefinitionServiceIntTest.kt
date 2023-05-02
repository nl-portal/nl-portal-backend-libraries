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
package com.ritense.portal.form.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.form.BaseIntegrationTest
import com.ritense.portal.form.domain.request.CreateFormDefinitionRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class FormIoFormDefinitionServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var formIoFormDefinitionService: FormIoFormDefinitionService

    @BeforeEach
    fun setup() {
    }

    @Transactional
    @Test
    fun `should create form `() {
        val createdEntity = formIoFormDefinitionService.createFormDefinition(
            CreateFormDefinitionRequest(
                "name",
                Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java),
                true
            )
        )

        assertThat(createdEntity.isNew).isTrue
    }

    @Transactional
    @Test
    fun `should retrieve all forms `() {
        val forms = formIoFormDefinitionService.findAllFormDefinitions()

        assertThat(forms).isNotEmpty
    }

    @Transactional
    @Test
    fun `should retrieve one form by name `() {
        val form = formIoFormDefinitionService.findFormIoFormDefinition("form-example")
        assertThat(form?.name).isEqualTo("form-example")
        assertThat(form?.isNew).isFalse
    }

    @Transactional
    @Test
    fun `should modify form definition of form `() {
        val newFormDef = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java)
        val form = formIoFormDefinitionService.findFormIoFormDefinition("form-example")
        form?.modifyFormDefinition(newFormDef)

        val modifiedForm = formIoFormDefinitionService.modify(form!!)
        assertThat(form.formDefinitionId).isEqualTo(modifiedForm.formDefinitionId)
        assertThat(modifiedForm.formDefinition).isEqualTo(newFormDef)
    }
}