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
package com.ritense.portal.case.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.portal.case.domain.meta.MetaJsonSchemaV7Draft
import com.ritense.portal.core.util.ObjectValidator
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.json.JSONObject

@Embeddable
data class Schema(

    @Column(name = "`schema`", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    var value: ObjectNode,
) {
    init {
        ObjectValidator.validate(this)
        MetaJsonSchemaV7Draft.validate(JSONObject(value.toString()))
    }

    fun validateCase(caseData: JSONObject) {
        if (caseData.isEmpty) {
            throw IllegalStateException("Empty case data")
        }
        // If there are some properties missing from input which have "default" values in the schema,
        // then they will be set by the validator during validation.
        val schema = SchemaLoaderBuilder.get()
            .schemaJson(JSONObject(value.toString()))
            .build()
            .load()
            .build()
        Validator.get().performValidation(schema, caseData)
    }
}