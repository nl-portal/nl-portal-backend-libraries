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
package nl.nlportal.form.startform.autodeployment

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import nl.nlportal.form.startform.domain.StartForm
import nl.nlportal.form.startform.service.StartFormService
import org.apache.commons.io.IOUtils
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

class StartFormDeploymentService(
    private val objectMapper: ObjectMapper,
    private val startFormService: StartFormService,
    private val resourceLoader: ResourceLoader,
) {
    fun deployAllFromResourceFiles() =
        try {
            val resources = loadResources()
            for (resource in resources) {
                if (resource.filename == null) {
                    continue
                }
                val name = getStartFormName(resource)
                val form = startFormService.findStartFormByFormName(name)
                if (form == null) {
                    val startForm = objectMapper.readValue(
                        IOUtils.toString(resource.inputStream, StandardCharsets.UTF_8),
                        StartForm::class.java,
                    )
                    startFormService.createStartForm(startForm)
                }
            }
        } catch (e: IOException) {
            logger.debug { "something went wrong while reading and saving the start form definitions due to: ${e.message}" }
        }

    private fun getStartFormName(resource: Resource): String {
        var formName = resource.filename
        if (formName != null && formName.endsWith(".json")) {
            formName = formName.substring(0, formName.length - 5)
        }
        return formName!!
    }

    @Throws(IOException::class)
    private fun loadResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(
            PATH,
        )
    }

    companion object {
        const val PATH = "classpath*:config/startform/*.json"
        private val logger = KotlinLogging.logger {}
    }
}