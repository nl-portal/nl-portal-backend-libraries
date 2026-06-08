/*
 * Copyright 2015-2026 Ritense BV, the Netherlands.
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
package nl.nlportal.documentenapi.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FilenameSanitizer {
    private val UNSAFE_FILENAME_CHARS = Regex("[/\\\\<>:\"|?*\\x00-\\x1f]")
    private val PATH_TRAVERSAL = Regex("(^|[/\\\\])\\.\\.")
    private val ONLY_DOTS = Regex("^\\.+$")
    private const val MAX_FILENAME_LENGTH = 255

    fun sanitize(filename: String?): String {
        if (filename.isNullOrBlank()) return "unnamed"

        var sanitized = filename
            .replace(UNSAFE_FILENAME_CHARS, "_")
            .replace(PATH_TRAVERSAL, "_")
            .trim('.', ' ')

        if (sanitized.isBlank() || sanitized.matches(ONLY_DOTS)) {
            sanitized = "unnamed"
        }

        if (sanitized.length > MAX_FILENAME_LENGTH) {
            val extIndex = sanitized.lastIndexOf('.')
            if (extIndex > 0 && sanitized.length - extIndex <= 10) {
                val ext = sanitized.substring(extIndex)
                sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH - ext.length) + ext
            } else {
                sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH)
            }
        }

        return sanitized
    }

    fun encodeForContentDisposition(filename: String?): String {
        val safe = sanitize(filename)

        val isAscii = safe.all { it.code in 32..126 && it != '"' && it != '\\' }

        return if (isAscii) {
            "attachment; filename=\"$safe\""
        } else {
            val encoded = URLEncoder.encode(safe, StandardCharsets.UTF_8)
                .replace("+", "%20")
            "attachment; filename=\"${safe.filter { it.code in 32..126 && it != '"' }}\"; filename*=UTF-8''$encoded"
        }
    }
}
