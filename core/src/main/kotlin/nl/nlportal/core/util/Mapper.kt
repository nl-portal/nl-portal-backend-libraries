/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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
package nl.nlportal.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatter

object Mapper {
    private var mapper: ObjectMapper
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    private val jacksonBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.simpleDateFormat(DATE_TIME_FORMAT)
            builder.serializers(LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        }
    private val jacksonConfigurationModule =
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()

    fun get(): ObjectMapper {
        return mapper
    }

    init {
        val builder = Jackson2ObjectMapperBuilder()
        jacksonBuilderCustomizer.customize(builder)

        mapper =
            builder.build<ObjectMapper>()
                .apply {
                    registerModule(jacksonConfigurationModule)
                }
    }
}