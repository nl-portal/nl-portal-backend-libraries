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

import java.security.NoSuchAlgorithmException
import java.util.UUID
import org.apache.commons.codec.digest.DigestUtils

object CoreUtils {
    @JvmStatic
    fun extractId(url: String): UUID {
        return UUID.fromString(url.substringAfterLast("/"))
    }

    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun createHash(
        input: String,
        shaVersion: String,
    ): String {
        return when (shaVersion) {
            ShaVersion.SHA256.version -> {
                DigestUtils.sha512Hex(input)
            }
            ShaVersion.SHA512.version -> {
                DigestUtils.sha512Hex(input)
            }
            else -> {
                DigestUtils.sha1Hex(input)
            }
        }
    }
}