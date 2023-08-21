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
package com.ritense.portal.form.autodeployment

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.core.util.Mapper
import com.ritense.portal.form.domain.request.CreateFormDefinitionRequest
import com.ritense.portal.form.service.FormIoFormDefinitionService
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

class FormDefinitionDeploymentService(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val resourceLoader: ResourceLoader
) {

    fun deployAllFromResourceFiles() = try {
        val resources = loadResources()
        for (resource in resources) {
            if (resource.filename == null) {
                continue
            }
            val name = getFormName(resource)
            val rawFormDefinition = getJson(IOUtils.toString(resource.inputStream, StandardCharsets.UTF_8))
            val form = formIoFormDefinitionService.findFormIoFormDefinitionByName(name)
            if (form == null) {
                formIoFormDefinitionService.createFormDefinition(
                    CreateFormDefinitionRequest(
                        name,
                        rawFormDefinition,
                        true
                    )
                )
            } else {
                if (form.formDefinition != rawFormDefinition) {
                    form.modifyFormDefinition(rawFormDefinition)
                    formIoFormDefinitionService.modify(form)
                }
            }
        }
    } catch (e: IOException) {
        logger.debug { "something went wrong while reading and saving the form definitions due to: ${e.message}" }
    }

    @Throws(JsonProcessingException::class)
    private fun getJson(jsonString: String): ObjectNode {
        return Mapper.get().readValue(jsonString, ObjectNode::class.java)
    }

    private fun getFormName(resource: Resource): String {
        var formName = resource.filename
        if (formName != null && formName.endsWith(".json")) {
            formName = formName.substring(0, formName.length - 5)
        }
        return formName!!
    }

    @Throws(IOException::class)
    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            PATH
        )
    }

    companion object {
        const val PATH = "classpath*:config/form/*.json"
        private val logger = KotlinLogging.logger {}
    }
}