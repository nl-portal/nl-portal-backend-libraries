import io.spring.gradle.dependencymanagement.org.codehaus.plexus.interpolation.os.Os.FAMILY_MAC
import org.apache.tools.ant.taskdefs.condition.Os

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

dockerCompose {
    if (Os.isFamily(FAMILY_MAC)) {
        executable = "/usr/local/bin/docker-compose"
        dockerExecutable = "/usr/local/bin/docker"
    }
    projectNamePrefix = "form"
    isRequiredBy(tasks.getByName("test"))
    useComposeFiles.addAll("../docker-resources/docker-compose-base-test.yml", "docker-compose-override.yml")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(project(":core"))
    api(project(":data"))
    api(project(":graphql"))
    api(project(":zgw:objectenapi"))

    testImplementation("org.springframework.boot", "spring-boot-starter-test")
    testImplementation("org.assertj", "assertj-core")
    testImplementation(TestDependencies.postgresql)
}

val jar: Jar by tasks
val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
bootJar.enabled = false
jar.enabled = true

apply(from = "gradle/publishing.gradle.kts")