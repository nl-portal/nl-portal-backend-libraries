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
package nl.nlportal.form.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import nl.nlportal.form.domain.FormDefinitionId
import nl.nlportal.form.domain.FormIoFormDefinition
import nl.nlportal.form.domain.request.CreateFormDefinitionRequest
import nl.nlportal.form.repository.FormIoFormDefinitionRepository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class FormIoFormDefinitionService(
    private val formIoFormDefinitionRepository: FormIoFormDefinitionRepository,
) {
    fun createFormDefinition(request: CreateFormDefinitionRequest): FormIoFormDefinition {
        val formIoFormDefinition = FormIoFormDefinition(
            FormDefinitionId.newId(
                UUID.randomUUID(),
            ),
            request.getName(),
            request.getFormDefinition(),
            request.isReadOnly(),
        )
        return formIoFormDefinitionRepository.saveAndFlush(
            formIoFormDefinition
        )
    }


    fun findAllFormDefinitions(): List<FormIoFormDefinition> = formIoFormDefinitionRepository.findAll()

    fun findFormIoFormDefinitionByName(name: String): FormIoFormDefinition? {
        //try {
            return formIoFormDefinitionRepository.findByName(name)
        //} catch (e: Exception) {
        //    logger.debug { e.message }
        //}
        //return null
    }

    fun modify(form: FormIoFormDefinition): FormIoFormDefinition = formIoFormDefinitionRepository.saveAndFlush(form)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
