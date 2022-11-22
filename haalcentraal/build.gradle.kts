/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

val isLib = true

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(project(":graphql"))
    api(project(":common-ground-authentication"))

    // jjwt
    implementation("io.jsonwebtoken", "jjwt-api", "0.11.2")
    implementation("io.jsonwebtoken", "jjwt-impl", "0.11.2")
    implementation("io.jsonwebtoken", "jjwt-jackson", "0.11.2")

    testImplementation(project(":common-ground-authentication-test"))
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.springframework.security", "spring-security-test")
    testImplementation("org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.5.2")
    testImplementation("com.nhaarman.mockitokotlin2", "mockito-kotlin", "2.2.0")
    testImplementation("com.squareup.okhttp3", "mockwebserver", "4.9.3")
    testImplementation("com.squareup.okhttp3", "okhttp", "4.9.3")
    testImplementation("com.squareup.okhttp3", "okhttp-tls", "4.9.3")
    testImplementation("org.hamcrest", "hamcrest", "2.2")
}

val jar: Jar by tasks
val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
bootJar.enabled = false
jar.enabled = true