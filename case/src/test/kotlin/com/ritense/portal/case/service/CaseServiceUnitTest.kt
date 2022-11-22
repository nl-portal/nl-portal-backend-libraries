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

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.BaseTest
import com.ritense.portal.case.domain.Case
import com.ritense.portal.case.domain.CaseDefinitionId
import com.ritense.portal.case.domain.CaseId
import com.ritense.portal.case.domain.Status
import com.ritense.portal.case.domain.Submission
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.messaging.`in`.UpdateExternalIdPortalCaseMessage
import com.ritense.portal.messaging.`in`.UpdateStatusPortalCaseMessage
import org.assertj.core.api.Assertions.assertThat
import org.everit.json.schema.ValidationException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication
import java.util.Optional
import java.util.UUID

class CaseServiceUnitTest : BaseTest() {
    lateinit var caseService: CaseService
    val initialStatus = "a"
    val user = "john@example.com"
    val caseDefinition = personCaseDefinition()
    val authentication = Mockito.mock(Authentication::class.java)

    @BeforeEach
    fun setup() {
        baseSetUp()
        `when`(caseDefinitionService.findById(caseDefinition.caseDefinitionId)).thenReturn(caseDefinition)
        caseService = CaseService(caseRepository, caseDefinitionService, sink)
        `when`(authentication.name).thenReturn(user)
    }

    @Test
    fun `should not create case with empty submission`() {
        val submission = Mapper.get().readValue("{}", ObjectNode::class.java)
        val illegalStateException = assertThrows(IllegalStateException::class.java) {
            caseService.create(
                caseDefinition.caseDefinitionId.value,
                submission,
                authentication
            )
        }
        assertThat(illegalStateException).hasMessage("Empty case data")
    }

    @Test
    fun `should eliminate unknown properties from submission prior case def validation`() {
        val submission = Mapper.get().readValue("{\"unknownProperty\" : \"myName\"}", ObjectNode::class.java)
        val illegalStateException = assertThrows(IllegalStateException::class.java) {
            caseService.create(
                caseDefinition.caseDefinitionId.value,
                submission,
                authentication
            )
        }
        assertThat(illegalStateException).hasMessage("Empty case data")
    }

    @Test
    fun `should not create case due to size validation exception`() {
        val submission = Mapper.get().readValue("{\"firstName\" : \"moreThan15CharsTooLong\"}", ObjectNode::class.java)
        val validationException = assertThrows(ValidationException::class.java) {
            caseService.create(
                caseDefinition.caseDefinitionId.value,
                submission,
                authentication
            )
        }
        assertThat(validationException).hasMessage("#/firstName: expected maxLength: 15, actual: 22")
    }

    @Test
    fun `should not create case due to type validation exception`() {
        val submission = Mapper.get().readValue("{\"firstName\" : 1}", ObjectNode::class.java)
        val validationException = assertThrows(ValidationException::class.java) {
            caseService.create(
                caseDefinition.caseDefinitionId.value,
                submission,
                authentication
            )
        }
        assertThat(validationException).hasMessage("#/firstName: expected type: String, found: Integer")
    }

    @Test
    fun `should create case with valid submission`() {
        val submission = Mapper.get().readValue("{\"firstName\" : \"myName\"}", ObjectNode::class.java)
        val case = caseService.create(
            caseDefinition.caseDefinitionId.value,
            submission,
            authentication
        )

        assertThat(case).isNotNull
        assertThat(case.caseId).isNotNull
        assertThat(case.userId).isEqualTo(user)
        assertThat(case.status.name).isEqualTo(initialStatus)
        assertThat(case.submission.value.path("firstName").textValue()).isEqualTo("myName")
    }

    @Test
    fun `should handle external case event`() {

        val externalCaseCreatedEvent = UpdateExternalIdPortalCaseMessage(UUID.randomUUID(), "anExternalId")

        val submissionData = JsonNodeFactory.instance.objectNode()
        submissionData.put("key", "value")

        `when`(caseRepository.findById(CaseId.existingId(externalCaseCreatedEvent.caseId))).thenReturn(
            Optional.of(
                Case(
                    caseId = CaseId.existingId(externalCaseCreatedEvent.caseId),
                    userId = "aUserName",
                    caseDefinitionId = CaseDefinitionId.existingId("aCaseDefinition"),
                    status = Status("in-progress"),
                    submission = Submission(submissionData)
                )
            )
        )

        val case = caseService.updateExternalId(externalCaseCreatedEvent)

        assertThat(case.externalId).isEqualTo(externalCaseCreatedEvent.externalId)
    }

    @Test
    fun `should not handle external case event if caseId is not found`() {
        val externalCaseCreatedEvent = UpdateExternalIdPortalCaseMessage(UUID.randomUUID(), "anExternalId")

        assertThrows(NullPointerException::class.java) {
            caseService.updateExternalId(externalCaseCreatedEvent)
        }
    }

    @Test
    fun `should not handle update status case event if external case id is not found`() {
        val externalCaseStatusUpdatedEvent = UpdateStatusPortalCaseMessage("anExternalId", "some status")

        assertThrows(NullPointerException::class.java) {
            caseService.updateStatus(externalCaseStatusUpdatedEvent)
        }
    }

    @Test
    fun `should handle status update case event`() {

        val event = UpdateStatusPortalCaseMessage("externalId", "b")

        val submissionData = JsonNodeFactory.instance.objectNode()
        submissionData.put("key", "value")

        `when`(caseRepository.findCaseByExternalId(event.externalId)).thenReturn(
            Case(
                caseId = CaseId.existingId(UUID.randomUUID()),
                userId = "aUserName",
                externalId = "externalId",
                status = Status("a"),
                caseDefinitionId = CaseDefinitionId.existingId("person"),
                submission = Submission(submissionData)
            )
        )
        `when`(caseDefinitionService.findById(CaseDefinitionId.existingId("person"))).thenReturn(
            personCaseDefinition()
        )

        val case = caseService.updateStatus(event)

        assertThat(case.status.name).isEqualTo(event.status)
        assertThat(case.statusHistory).hasSize(1)
        val historicStatus = case.statusHistory?.get(0)
        assertThat(historicStatus?.status?.name).isEqualTo("a")
        assertThat(historicStatus?.status?.createdOn).isNotNull
        assertThat(historicStatus?.createdOn).isNotNull
    }
}