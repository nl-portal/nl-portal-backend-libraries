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
    val hamcrest by lazy { "org.hamcrest:hamcrest:${Versions.hamcrest}" }
    val kotlinCoroutines by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"}
    val mockitoKotlin by lazy { "org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}" }
    val okHttp by lazy { "com.squareup.okhttp3:okhttp:${Versions.okHttp3}" }
    val okHttpMockWebserver by lazy { "com.squareup.okhttp3:mockwebserver:${Versions.okHttp3}" }
    val okHttpTls by lazy { "com.squareup.okhttp3:okhttp-tls:${Versions.okHttp3}" }
    val postgresql by lazy { "org.postgresql:postgresql:${Versions.postgresql}" }
    val springBootTest by lazy {"org.springframework.boot:spring-boot-starter-test"}
    val springSecurityTest by lazy {"org.springframework.security:spring-security-test"}
    val assertJCore by lazy { "org.assertj:assertj-core" }
    val kotlinTest by lazy { "org.jetbrains.kotlin:kotlin-test" }
    val junitJupiterTest by lazy { "org.junit.jupiter:junit-jupiter" }
}
