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

object Dependencies {
    val everitJsonSchema by lazy { "com.github.erosb:everit-json-schema:${Versions.everitJsonSchema}" }
    val jsonPath by lazy { "com.jayway.jsonpath:json-path:${Versions.jsonPath}" }
    val jsonWebTokensApi by lazy { "io.jsonwebtoken:jjwt-api:${Versions.jsonWebTokens}" }
    val jsonWebTokensImpl by lazy { "io.jsonwebtoken:jjwt-impl:${Versions.jsonWebTokens}" }
    val jsonWebTokensJackson by lazy { "io.jsonwebtoken:jjwt-jackson:${Versions.jsonWebTokens}" }
    val kotlinxCoroutinesReactor by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.kotlinxCoroutinesReactor}" }
}
