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
plugins {
    kotlin("jvm")
}

val jar: Jar by tasks
val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

bootJar.enabled = false
jar.enabled = true

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api("org.springframework.boot", "spring-boot-starter-validation")
    api("org.springframework.boot", "spring-boot-starter-security")
    api("org.springframework.boot", "spring-boot-starter-webflux")
    api("org.springframework.security", "spring-security-oauth2-jose")
    api("org.springframework.security", "spring-security-oauth2-resource-server")

    // Jackson
    api(ApiDependencies.jacksonBom)
    api("com.fasterxml.jackson.module", "jackson-module-kotlin")

    api(ApiDependencies.commonsCodec)

    // Liquibase
    api("org.liquibase", "liquibase-core")

    // Apache Commons
    api(ApiDependencies.commonsIo)
    api(ApiDependencies.apacheCommons)

    // Logging for Kotlin
    api(ApiDependencies.kotlinLogging)

    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.mockitoKotlin)
}

apply(from = "gradle/publishing.gradle.kts")