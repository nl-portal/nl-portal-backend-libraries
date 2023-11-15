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

object TestDependencies {
    val hamcrest by lazy { "org.hamcrest:hamcrest:${TestVersions.hamcrest}" }
    val kotlinCoroutines by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-test:${TestVersions.kotlinCoroutines}"}
    val mockitoKotlin by lazy { "org.mockito.kotlin:mockito-kotlin:${TestVersions.mockitoKotlin}" }
    val okHttp by lazy { "com.squareup.okhttp3:okhttp:${TestVersions.okHttp3}" }
    val okHttpMockWebserver by lazy { "com.squareup.okhttp3:mockwebserver:${TestVersions.okHttp3}" }
    val okHttpTls by lazy { "com.squareup.okhttp3:okhttp-tls:${TestVersions.okHttp3}" }
    val postgresql by lazy { "org.postgresql:postgresql:${TestVersions.postgresql}" }
}