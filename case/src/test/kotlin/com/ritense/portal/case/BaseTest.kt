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
package com.ritense.portal.case

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.domain.Case
import com.ritense.portal.case.domain.CaseDefinition
import com.ritense.portal.case.domain.CaseDefinitionId
import com.ritense.portal.case.domain.CaseId
import com.ritense.portal.case.domain.Schema
import com.ritense.portal.case.domain.Status
import com.ritense.portal.case.domain.StatusDefinition
import com.ritense.portal.case.domain.Submission
import com.ritense.portal.case.repository.CaseRepository
import com.ritense.portal.case.service.CaseDefinitionService
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.messaging.out.PortalMessage
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import reactor.core.publisher.Sinks
import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID

abstract class BaseTest {

    @Mock
    lateinit var caseRepository: CaseRepository

    @Mock
    lateinit var caseDefinitionService: CaseDefinitionService

    @Mock
    lateinit var sink: Sinks.Many<PortalMessage>

    fun baseSetUp() {
        MockitoAnnotations.openMocks(this)
    }

    fun getResourceAsStream(resource: String): InputStream {
        return Thread.currentThread().contextClassLoader.getResourceAsStream(resource)!!
    }

    fun personCaseDefinition(): CaseDefinition {
        val caseDefinitionId = CaseDefinitionId.existingId("person")
        val schema = Schema(
            Mapper.get().readValue(
                getResourceAsStream("config/case/definition/person/person.schema.json"),
                ObjectNode::class.java
            )
        )
        val statusDefinition = StatusDefinition(
            Mapper.get().readValue(
                getResourceAsStream("config/case/definition/person/status.json"),
                object : TypeReference<List<String>>() {}
            )
        )
        val caseDefinition = CaseDefinition(
            caseDefinitionId = caseDefinitionId,
            schema = schema,
            statusDefinition = statusDefinition
        )
        return caseDefinition
    }

    fun case(createdOn: LocalDateTime): Case {
        return Case(
            createdOn = createdOn,
            caseId = CaseId.existingId(UUID.randomUUID()),
            userId = "aUserName",
            externalId = "externalId",
            status = Status("a"),
            caseDefinitionId = CaseDefinitionId.existingId("person"),
            submission = Submission(JsonNodeFactory.instance.objectNode())
        )
    }
}