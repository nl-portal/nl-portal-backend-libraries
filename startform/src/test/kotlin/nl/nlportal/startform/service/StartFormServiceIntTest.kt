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
package nl.nlportal.startform.service

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import kotlinx.coroutines.runBlocking
import nl.nlportal.startform.BaseIntegrationTest
import nl.nlportal.startform.domain.StartForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@Transactional
class StartFormServiceIntTest : BaseIntegrationTest() {
    @Autowired
    lateinit var startFormService: StartFormService

    @Test
    fun `create startform`() {
        val startFormsInDBBefore = startFormService.getAllStartFormDTOs()
        createStartForm()
        val startFormsInDBAfter = startFormService.getAllStartFormDTOs()
        assertThat(startFormsInDBBefore.size).isLessThan(startFormsInDBAfter.size)
    }

    @Test
    fun `find startform by name`() {
        createStartForm()
        val startFormInDB = startFormService.findStartFormByFormName("formName")
        assertThat(startFormInDB).isNotNull
    }

    @Test
    fun `getStartform by name should throw NoSuchElementException`() {
        runBlocking {
            createStartForm()
            assertThrows<EntityNotFoundException> {
                startFormService.getStartFormByFormName("Wrong formName")
            }
        }
    }

    @Test
    fun `find no startform by name`() {
        createStartForm()
        val startFormInDB = startFormService.findStartFormByFormName("Wrong formName")
        assertThat(startFormInDB).isNull()
    }

    private fun createStartForm(): StartForm {
        return startFormService.createStartForm(
            StartForm(
                id = UUID.randomUUID(),
                formName = "formName",
                typeUUID = UUID.randomUUID(),
                typeVersion = 1,
            ),
        )
    }
}