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
package nl.nlportal.case.domain.meta

import nl.nlportal.case.BaseTest
import org.assertj.core.api.Assertions.assertThatCode
import org.everit.json.schema.ValidationException
import org.json.JSONObject
import org.json.JSONTokener
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetaJsonSchemaV7DraftUnitTest : BaseTest() {
    @BeforeEach
    fun setup() {
        baseSetUp()
    }

    @Test
    fun `should not return valid schema`() {
        val subject = JSONObject(JSONTokener(getResourceAsStream("config/case/definition/invalidperson.schema.json")))
        assertThrows(ValidationException::class.java) {
            MetaJsonSchemaV7Draft.validate(subject)
        }
    }

    @Test
    fun `should not return valid schema for empty schema`() {
        assertThrows(IllegalStateException::class.java) {
            MetaJsonSchemaV7Draft.validate(JSONObject(JSONTokener("{}")))
        }
    }

    @Test
    fun `should return valid schema`() {
        val subject = JSONObject(JSONTokener(getResourceAsStream("config/case/definition/person/person.schema.json")))
        assertThatCode {
            MetaJsonSchemaV7Draft.validate(subject)
        }.doesNotThrowAnyException()
    }
}