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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.domain.CaseDefinition
import com.ritense.portal.case.domain.CaseDefinitionId
import com.ritense.portal.case.domain.Schema
import com.ritense.portal.case.domain.StatusDefinition
import com.ritense.portal.case.repository.CaseDefinitionRepository
import com.ritense.portal.core.util.Mapper
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets

@Transactional
class CaseDefinitionService(
    private val caseDefinitionRepository: CaseDefinitionRepository,
    private val resourceLoader: ResourceLoader
) {

    fun findById(caseDefinitionId: CaseDefinitionId): CaseDefinition? {
        return caseDefinitionRepository.findByCaseDefinitionId(caseDefinitionId)
    }

    fun deploy(caseSchema: ObjectNode, statuses: List<String>) {
        val id = retrieveIdFrom(caseSchema)
        val existingCaseDefinition = caseDefinitionRepository.findByCaseDefinitionId(id)
        if (existingCaseDefinition == null) {
            val caseDefinition = CaseDefinition(
                caseDefinitionId = retrieveIdFrom(caseSchema),
                schema = Schema(caseSchema),
                statusDefinition = StatusDefinition(statuses)
            )
            caseDefinitionRepository.save(caseDefinition)
        } else if (existingCaseDefinition.schema.value != caseSchema) {
            existingCaseDefinition.modify(caseSchema)
            caseDefinitionRepository.save(existingCaseDefinition)
        }
    }

    fun deployAll() {
        logger.info("Deploying all case definition's")
        val resources: Array<Resource> = loadCaseResources()
        for (resource in resources) {
            val resourcePath = resource.url.path.split('/')
            val resourceDir = resourcePath[resourcePath.size - 2]
            val statusResource = loadCaseStatusResource(resourceDir)

            if (resource.filename != null) {
                try {
                    deploy(
                        Mapper.get().readValue(
                            StreamUtils.copyToString(
                                resource.inputStream,
                                StandardCharsets.UTF_8
                            ),
                            ObjectNode::class.java
                        ),
                        Mapper.get().readValue(
                            StreamUtils.copyToString(
                                statusResource.inputStream,
                                StandardCharsets.UTF_8
                            ),
                            object : TypeReference<List<String>>() {}
                        )
                    )
                } catch (ex: Exception) {
                    logger.error("Error deploying case definition's", ex)
                }
            }
        }
    }

    private fun loadCaseResources(): Array<Resource> {
        return getResources(CASE_PATH)
    }

    private fun loadCaseStatusResource(caseDir: String): Resource {
        return getResources("classpath*:config/case/definition/$caseDir/status.json")[0]
    }

    private fun getResources(path: String): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            path
        )
    }

    private fun retrieveIdFrom(schema: ObjectNode): CaseDefinitionId {
        val id = StringUtils.substringBefore(schema.get("\$id").asText(), ".schema").lowercase()
        return CaseDefinitionId.newId(id)
    }

    fun getAllCaseDefinitions(): List<CaseDefinition> {
        return caseDefinitionRepository.findAll()
    }

    companion object {
        const val CASE_PATH = "classpath*:config/case/definition/*/*.schema.json"
        private val logger = KotlinLogging.logger {}
    }
}