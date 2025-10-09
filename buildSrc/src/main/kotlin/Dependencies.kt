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
    val kotlinCoroutines by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}" }
    val kotlinCoroutinesReactor by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.kotlinCoroutines}" }
    val springCloudConfig by lazy { "org.springframework.cloud:spring-cloud-starter-config:${Versions.springCloud}" }
    val springCloudBootstrap by lazy { "org.springframework.cloud:spring-cloud-starter-bootstrap:${Versions.springCloud}" }

    val clamAv by lazy { "xyz.capybara:clamav-client:${Versions.clamAvVersion}" }
    val tikaCore by lazy { "org.apache.tika:tika-core:${Versions.apacheTikaVersion}" }

    val commonsIo by lazy { "commons-io:commons-io:${Versions.commonsIo}" }
    val commonsCodec by lazy { "commons-codec:commons-codec:${Versions.commonsCodec}" }
    val apacheCommons by lazy { "org.apache.commons:commons-lang3:${Versions.apacheCommons}"}
    val graphqlJavaExtendedScalars by lazy { "com.graphql-java:graphql-java-extended-scalars:${Versions.graphqlJava}"}
    //val graphqlKotlinHooksProvider by lazy { "com.expediagroup:graphql-kotlin-hooks-provider:${Versions.graphqlKotlin}"}
    //val graphqlKotlinSpringServer by lazy { "com.expediagroup:graphql-kotlin-spring-server:${Versions.graphqlKotlin}"}

    val jacksonBom by lazy { "com.fasterxml.jackson:jackson-bom:${Versions.jacksonBom}" }
    val kotlinLogging by lazy { "io.github.oshai:kotlin-logging-jvm:${Versions.kotlinLogging}"}
    val springCloudStream by lazy { "org.springframework.cloud:spring-cloud-stream:${Versions.springCloud}" }
    val springCloudStreamBinderRabbit by lazy { "org.springframework.cloud:spring-cloud-stream-binder-rabbit:${Versions.springCloud}" }
    val springBootStarter by lazy {"org.springframework.boot:spring-boot-starter"}
}
