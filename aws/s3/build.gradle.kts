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

group = "org.example"
version = "1.0.8.RELEASE"

repositories {
    mavenCentral()
}

dependencies {
//    api(project(":graphql"))
    api(project(":core"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-web")
    implementation("com.amazonaws:aws-java-sdk:1.12.472")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation(project(mapOf("path" to ":core")))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.assertj", "assertj-core")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val jar: Jar by tasks
val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
bootJar.enabled = false
jar.enabled = true

dockerCompose {
    projectNamePrefix = "aws-s3"
    isRequiredBy(tasks.getByName("test"))
    useComposeFiles.addAll("../s3/src/test/resources/docker-resources/docker-compose.yml")
}