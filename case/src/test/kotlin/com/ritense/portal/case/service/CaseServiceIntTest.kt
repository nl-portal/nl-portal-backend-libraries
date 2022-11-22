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
package com.ritense.portal.case.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.BaseIntegrationTest
import com.ritense.portal.core.util.Mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional

@Transactional
class CaseServiceIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var caseService: CaseService

    @Autowired
    lateinit var caseDefinitionService: CaseDefinitionService

    val initialStatus = "a"
    val user = "john@example.com"
    val authentication = Mockito.mock(Authentication::class.java)

    @BeforeEach
    fun setup() {
        caseDefinitionService.deployAll()
        Mockito.`when`(authentication.name).thenReturn(user)
    }

    @Test
    fun `should not create case with empty submission`() {
        val submission = Mapper.get().readValue("{}", ObjectNode::class.java)
        val illegalStateException = assertThrows(IllegalStateException::class.java) {
            caseService.create(
                "person",
                submission,
                authentication,
                initialStatus
            )
        }
        assertThat(illegalStateException).hasMessage("Empty case data")
    }

    @Test
    fun `should create case with valid submission`() {
        val submission = Mapper.get().readValue("{\"firstName\" : \"myName\", \"extra-key-non-portal-property\": \"value\"}", ObjectNode::class.java)
        val extraProperty = Mapper.get().readValue("{\"extra-key-non-portal-property\": \"value\"}", ObjectNode::class.java)

        val case = caseService.create(
            "person",
            submission,
            authentication,
            initialStatus
        )

        assertThat(case).isNotNull
        assertThat(case.caseId).isNotNull
        assertThat(case.userId).isEqualTo(user)
        assertThat(case.status.name).isEqualTo(initialStatus)
        assertThat(case.submission.value.path("firstName").textValue()).isEqualTo("myName")
        assertThat(case.submission.value.contains(extraProperty)).isFalse
    }
}