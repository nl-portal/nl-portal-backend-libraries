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

dockerCompose {
    setProjectName("$name-test")
    isRequiredBy(tasks.getByName("integrationTest"))
    useComposeFiles.addAll("../../docker-resources/docker-compose-base-test.yml", "docker-compose-override.yml")
}

dependencies {
    api(project(":graphql"))
    api(project(":portal-authentication"))
    api(project(":zgw:common-ground-authentication"))

    testImplementation(project(":zgw:common-ground-authentication-test"))
    testImplementation(TestDependencies.postgresql)
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.springframework.security", "spring-security-test")
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.mockitoKotlin)
    testImplementation(TestDependencies.okHttpMockWebserver)
    testImplementation(TestDependencies.okHttp)
}

val jar: Jar by tasks
val bootJar: BootJar by tasks
bootJar.enabled = false
jar.enabled = true

apply(from = "gradle/publishing.gradle.kts")