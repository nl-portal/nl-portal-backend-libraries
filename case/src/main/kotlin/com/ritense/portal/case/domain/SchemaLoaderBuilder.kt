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
package com.ritense.portal.case.domain

import com.ritense.portal.case.config.UuidFormatValidator
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader

object SchemaLoaderBuilder {

    private val DEFAULT_REFERENCE_PATH_LOCATION = "classpath://config/case/definition/reference/"

    private val schemaLoaderBuilder = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope(DEFAULT_REFERENCE_PATH_LOCATION)
        .useDefaults(true)
        .draftV7Support()
        .addFormatValidator(UuidFormatValidator())

    fun get(): SchemaLoader.SchemaLoaderBuilder {
        return schemaLoaderBuilder
    }
}