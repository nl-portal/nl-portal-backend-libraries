import org.springframework.boot.gradle.tasks.bundling.BootJar
/*
 * Copyright (c) 2024 Ritense BV, the Netherlands.
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
    api(project(":graphql"))
    api(project(":portal-authentication"))
    api(project(":zgw:common-ground-authentication"))
    api(project(":zgw:idtoken-authentication"))

    implementation(Dependencies.kotlinCoroutines)
    implementation(Dependencies.kotlinCoroutinesReactor)
    implementation("org.springframework.data:spring-data-commons")

    testImplementation(project(":zgw:common-ground-authentication-test"))
    testImplementation(TestDependencies.postgresql)
    testImplementation(TestDependencies.springBootTest)
    testImplementation(TestDependencies.springSecurityTest)
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.mockitoKotlin)
    testImplementation(TestDependencies.okHttpMockWebserver)
    testImplementation(TestDependencies.okHttp)
    testImplementation("org.springframework.graphql:spring-graphql-test")
}

val jar: Jar by tasks
val bootJar: BootJar by tasks
bootJar.enabled = false
jar.enabled = true

apply(from = "gradle/publishing.gradle.kts")
