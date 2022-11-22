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
package com.ritense.portal.form.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.core.util.Mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class FormIoFormDefinitionTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun createEntity() {
        val formDef = Mapper.get().readValue("{\"display\": \"form\"}", ObjectNode::class.java)
        val formIoFormDefinition =
            FormIoFormDefinition(
                FormDefinitionId.newId(UUID.randomUUID()),
                "name",
                formDef,
                true
            )

        assertThat(formIoFormDefinition.readOnly).isTrue
        assertThat(formIoFormDefinition.name).isEqualTo("name")
        assertThat(formIoFormDefinition.formDefinition).isEqualTo(formDef)
        assertThat(formIoFormDefinition.formDefinitionId.isNew()).isTrue
    }
}