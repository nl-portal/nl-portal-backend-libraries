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
    setProjectName("$name-test")
    isRequiredBy(tasks.getByName("integrationTest"))
    useComposeFiles.addAll("../docker-resources/docker-compose-base-test.yml", "docker-compose-override.yml")
}

dependencies {
    implementation(project(":zgw:openklant"))
    implementation(project(":form"))
    implementation(project(":haalcentraal-hr"))
    implementation(project(":haalcentraal2"))
    implementation(project(":zgw:taak"))
    implementation(project(":zgw:documenten-api"))
    implementation(project(":zgw:berichten"))
    implementation(project(":zgw:besluiten"))
    implementation(project(":payment"))
    implementation(project(":payment-direct"))
    implementation(project(":zgw:zaken-api"))
    implementation(project(":zgw:openproduct"))
    implementation(project(":product"))
    implementation(project(":zgw:verificatie"))
    implementation(project(":case"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api("org.postgresql:postgresql")

    testImplementation(project(":zgw:common-ground-authentication-test"))
    testImplementation(TestDependencies.postgresql)
    testImplementation(TestDependencies.springBootTest)
    testImplementation(TestDependencies.springSecurityTest)
    testImplementation(TestDependencies.assertJCore)
    testImplementation(TestDependencies.kotlinCoroutines)
    testImplementation(TestDependencies.okHttpMockWebserver)
    testImplementation("org.springframework.graphql:spring-graphql-test")
}

tasks.withType<Jar>().configureEach {
    enabled = false
}

tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
}
tasks.withType<PublishToMavenLocal>().configureEach {
    enabled = false
}
