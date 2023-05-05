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
package com.ritense.portal.case.domain.meta

import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import java.io.InputStream

object MetaJsonSchemaV7Draft {

    private const val DRAFT_V7_RESOURCE_NAME = "meta-schema/draftv7.json"
    private val DRAFT_V7 = JSONObject(JSONTokener(getResourceAsStream(DRAFT_V7_RESOURCE_NAME)))
    private var schema = SchemaLoader
        .builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .useDefaults(true)
        .draftV7Support()
        .schemaJson(DRAFT_V7)
        .build()
        .load()
        .build()

    fun validate(subject: JSONObject) {
        if (subject.isEmpty) {
            throw IllegalStateException("Validating empty schema")
        }
        this.schema.validate(subject)
    }

    fun getResourceAsStream(resource: String): InputStream {
        return Thread.currentThread().contextClassLoader.getResourceAsStream(resource)!!
    }
}