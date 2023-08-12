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
package com.ritense.valtimo.portal.form.service


import com.ritense.valtimo.portal.form.domain.ObjectsApiFormIoFormDefinition
import nl.nlportal.zgw.objectenapi.service.ObjectenApiService
import org.springframework.transaction.annotation.Transactional

@Transactional
class ObjectsApiFormDefinitionService(
    private val objectenApiService: ObjectenApiService
) {
    suspend fun findObjectsApiFormDefinition(objectUrl: String): ObjectsApiFormIoFormDefinition? {
        return objectenApiService.getObjectByUrl<ObjectsApiFormIoFormDefinition>(objectUrl)?.record?.data
    }
}