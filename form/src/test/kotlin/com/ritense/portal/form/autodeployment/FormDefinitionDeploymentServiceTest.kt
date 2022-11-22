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
package com.ritense.portal.form.autodeployment

import com.ritense.portal.form.domain.request.CreateFormDefinitionRequest
import com.ritense.portal.form.service.FormIoFormDefinitionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.core.io.ResourceLoader

internal class FormDefinitionDeploymentServiceTest {

    @Mock
    lateinit var formDefinitionService: FormIoFormDefinitionService
    @Mock
    lateinit var resourceLoader: ResourceLoader

    lateinit var formDefinitionDeploymentService: FormDefinitionDeploymentService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        formDefinitionDeploymentService = FormDefinitionDeploymentService(formDefinitionService, resourceLoader)
    }

    inline fun <reified T> anyNonNull(): T = Mockito.any<T>(T::class.java)

    @Test
    fun `should deploy form from resource folder`() {
        formDefinitionDeploymentService.deployAllFromResourceFiles()

        verify(formDefinitionService, times(1)).createFormDefinition(anyNonNull<CreateFormDefinitionRequest>())
    }
}