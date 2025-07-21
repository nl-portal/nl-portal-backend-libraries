/*
 * Copyright 2025 Ritense BV, the Netherlands.
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
package nl.nlportal.core.frontend

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.autoconfiguration.CoreThemeConfiguration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/public"])
class FrontendConfigurationResource(
    private val coreThemeConfiguration: CoreThemeConfiguration,
) {
    init {
        logger.info { "Start FrontendConfigurationResource with $coreThemeConfiguration" }
    }

    @GetMapping(value = ["/theme/style"])
    fun style(): ResponseEntity<String> {
        if (!coreThemeConfiguration.style.isNullOrBlank()) {
            return ResponseEntity.ok(coreThemeConfiguration.style)
        }

        return ResponseEntity.noContent().build()
    }

    @GetMapping(value = ["/theme/logo"])
    fun logo(): ResponseEntity<String> {
        if (!coreThemeConfiguration.style.isNullOrBlank()) {
            return ResponseEntity.ok(coreThemeConfiguration.logo)
        }

        return ResponseEntity.noContent().build()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}