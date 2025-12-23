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

val isLib = true

dependencies {

    api(project(":graphql"))
    api(project(":zgw:idtoken-authentication"))
    api(project(":zgw:catalogi-api"))

    implementation(Dependencies.springBootStarter)

    implementation(Dependencies.kotlinCoroutines)
    implementation(Dependencies.kotlinCoroutinesReactor)
    implementation("org.springframework.data:spring-data-commons")

    testImplementation(TestDependencies.kotlinTest)
    testImplementation(TestDependencies.junitJupiterTest)
    testImplementation(TestDependencies.springBootTest)
    testImplementation(TestDependencies.springBootWebClientTest)
    testImplementation(TestDependencies.springBootTestWebClient)
    testImplementation(TestDependencies.springGraphQLTest)
    testImplementation(TestDependencies.springSecurityTest)

    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.mockitoKotlin)
    testImplementation(TestDependencies.okHttpMockWebserver)
    testImplementation(TestDependencies.okHttp)
    testImplementation(TestDependencies.postgresql)
    testImplementation(project(":zgw:common-ground-authentication-test"))
}

val jar: Jar by tasks
val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
bootJar.enabled = false
jar.enabled = true

apply(from = "gradle/publishing.gradle.kts")
