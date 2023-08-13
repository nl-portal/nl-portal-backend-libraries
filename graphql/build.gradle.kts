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
    api(project(":core"))

    // GraphQL
    api(ApiDependencies.graphqlKotlinSpringServer)
    api(ApiDependencies.graphqlKotlinHooksProvider)
    api(ApiDependencies.graphqlJavaExtendedScalars)

    // Jackson
    api("com.fasterxml.jackson.module", "jackson-module-kotlin")

    // Logging for Kotlin
    api(ApiDependencies.kotlinLogging)

    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.springframework.security", "spring-security-test")
    testImplementation("org.springframework.boot", "spring-boot-starter-webflux")
    testImplementation("org.assertj", "assertj-core")
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.postgresql)
}

apply(from = "gradle/publishing.gradle.kts")