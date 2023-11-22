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
package nl.nlportal.case.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.nlportal.case.BaseIntegrationTest
import nl.nlportal.core.util.Mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseDefinitionServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var caseDefinitionService: CaseDefinitionService

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `should not deploy empty case definition`() {
        val caseSchema = Mapper.get().readValue("{}", ObjectNode::class.java)
        val statuses = Mapper.get().readValue("[]", object : TypeReference<List<String>>() {})
        assertThrows(Exception::class.java) {
            caseDefinitionService.deploy(caseSchema, statuses)
        }
    }

    @Test
    fun `should deploy case definition`() {
        val caseSchema = Mapper.get().readValue(getResourceAsStream("config/case/definition/person/person.schema.json"), ObjectNode::class.java)
        val statuses = Mapper.get().readValue(getResourceAsStream("config/case/definition/person/status.json"), object : TypeReference<List<String>>() {})

        caseDefinitionService.deploy(caseSchema, statuses)

        val caseDefinition = caseDefinitionService.getAllCaseDefinitions().first()

        assertThat(caseDefinition.caseDefinitionId).isNotNull
        assertThat(caseDefinition.caseDefinitionId.value).isEqualTo("person")
        assertThat(caseDefinition.createdOn).isNotNull
        assertThat(caseDefinition.schema).isNotNull
        assertThat(caseDefinition.statusDefinition).isNotNull
        assertThat(caseDefinition.statusDefinition.statuses).isEqualTo(statuses)
    }

    @Test
    fun `should get all case definitions`() {
        val allCaseDefinitions = caseDefinitionService.getAllCaseDefinitions()
        assertEquals(0, allCaseDefinitions.size)

        val caseSchema = Mapper.get().readValue(getResourceAsStream("config/case/definition/person/person.schema.json"), ObjectNode::class.java)
        val statuses = Mapper.get().readValue(getResourceAsStream("config/case/definition/person/status.json"), object : TypeReference<List<String>>() {})

        caseDefinitionService.deploy(caseSchema, statuses)
        val caseDefinitions = caseDefinitionService.getAllCaseDefinitions()

        assertNotEquals(0, caseDefinitions.size)
    }
}