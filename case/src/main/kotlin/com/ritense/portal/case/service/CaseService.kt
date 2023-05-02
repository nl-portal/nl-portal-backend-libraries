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
package com.ritense.portal.case.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.domain.Case
import com.ritense.portal.case.domain.CaseDefinition
import com.ritense.portal.case.domain.CaseDefinitionId
import com.ritense.portal.case.domain.CaseId
import com.ritense.portal.case.domain.Status
import com.ritense.portal.case.domain.Submission
import com.ritense.portal.case.repository.CaseRepository
import com.ritense.portal.messaging.`in`.UpdateExternalIdPortalCaseMessage
import com.ritense.portal.messaging.`in`.UpdatePortalCaseMessage
import com.ritense.portal.messaging.`in`.UpdateStatusPortalCaseMessage
import com.ritense.portal.messaging.out.CreateExternalCaseMessage
import com.ritense.portal.messaging.out.ExternalIdUpdatedConfirmationMessage
import com.ritense.portal.messaging.out.PortalMessage
import mu.KotlinLogging
import org.json.JSONObject
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Sinks
import java.util.UUID

@Transactional
class CaseService(
    private val caseRepository: CaseRepository,
    private val caseDefinitionService: CaseDefinitionService,
    private val sink: Sinks.Many<PortalMessage>
) {

    fun create(
        caseDefinitionId: String,
        submission: ObjectNode,
        authentication: Authentication,
        initialStatus: String? = null
    ): Case {
        val caseDefinition = caseDefinitionService.findById(CaseDefinitionId.existingId(caseDefinitionId))!!
        val messageData = submission.deepCopy()
        val caseData = validateSubmissionAgainstSchema(submission, caseDefinition)
        val status: Status = if (initialStatus == null) {
            Status(caseDefinition.statusDefinition.statuses.first())
        } else {
            caseDefinition.statusDefinition.validateStatus(initialStatus)
            Status(initialStatus)
        }
        val case = Case(
            caseId = CaseId.newId(UUID.randomUUID()),
            userId = authentication.name,
            status = status,
            submission = Submission(caseData),
            caseDefinitionId = caseDefinition.caseDefinitionId
        )
        caseRepository.save(case)
        sink.tryEmitNext(
            CreateExternalCaseMessage(
                case.caseId.value,
                messageData,
                caseDefinition.caseDefinitionId.value
            )
        )
        return case
    }

    fun getAllCases(userId: String): List<Case> {
        return caseRepository.findAllByUserId(userId)
    }

    fun getCase(id: CaseId, userId: String): Case? {
        return caseRepository.findCaseByCaseIdAndUserId(id, userId)
    }

    fun getCase(externalId: String): Case? {
        return caseRepository.findCaseByExternalId(externalId)
    }

    fun updateExternalId(updateExternalIdPortalCaseMessage: UpdateExternalIdPortalCaseMessage): Case {
        logger.debug { "Received create case with external id: ${updateExternalIdPortalCaseMessage.externalId}" }
        val case = caseRepository.findById(CaseId.existingId(updateExternalIdPortalCaseMessage.caseId))
            .orElseThrow { NullPointerException() }
        case.externalId = updateExternalIdPortalCaseMessage.externalId
        caseRepository.save(case)
        sendUpdateConfirmation(case.externalId!!)
        return case
    }

    fun updateStatus(event: UpdateStatusPortalCaseMessage): Case {
        logger.debug { "Received status update with status: ${event.status} with external id: ${event.externalId}" }
        val case = caseRepository.findCaseByExternalId(event.externalId)!!
        val caseDefinition = caseDefinitionService.findById(case.caseDefinitionId)!!
        caseDefinition.statusDefinition.validateStatus(event.status)
        case.changeStatus(Status(event.status))
        caseRepository.save(case)
        return case
    }

    fun updateCase(event: UpdatePortalCaseMessage): Case {
        logger.debug { "Received case update with external id: ${event.externalId}" }
        val case = caseRepository.findCaseByExternalId(event.externalId)!!
        case.updateSubmission(event.properties)

        val caseDefinition = caseDefinitionService.findById(case.caseDefinitionId)!!
        caseDefinition.schema.validateCase(JSONObject(case.submission.value.toString()))

        caseRepository.save(case)
        return case
    }

    private fun validateSubmissionAgainstSchema(
        submission: ObjectNode,
        caseDefinition: CaseDefinition
    ): ObjectNode {
        logger.debug { "Validating submission against json schema: ${caseDefinition.caseDefinitionId.value}" }
        val keys = getKeys(caseDefinition.schema.value, mutableListOf())

        submission.retain(keys)
        caseDefinition.schema.validateCase(JSONObject(submission.toString()))

        return submission
    }

    private fun getKeys(json: JsonNode, keys: MutableList<String>): List<String> {
        if (json.isObject) {
            json.fields().forEachRemaining { field ->
                keys.add(field.key)
                getKeys(field.value, keys)
            }
        }
        if (json.isArray) {
            json.forEach { node -> getKeys(node, keys) }
        }
        if (json.isValueNode) {
            keys.add(json.asText())
        }
        return keys
    }

    private fun sendUpdateConfirmation(externalId: String) {
        sink.tryEmitNext(ExternalIdUpdatedConfirmationMessage(externalId))
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}