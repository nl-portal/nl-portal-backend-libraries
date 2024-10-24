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

dependencies {
    implementation(project(":haalcentraal:haalcentraal-all"))
    implementation(project(":klant"))
    implementation(project(":klantcontactmomenten"))
    implementation(project(":product"))
    implementation(project(":form"))
    implementation(project(":zgw:berichten"))
    implementation(project(":zgw:taak"))
    implementation(project(":zgw:zaken-api"))
    implementation(project(":zgw:catalogi-api"))
    implementation(project(":zgw:documenten-api"))
    implementation(project(":payment"))
    implementation(project(":zgw:besluiten"))

    implementation("org.springframework.boot", "spring-boot-starter-actuator")
    api("org.postgresql", "postgresql")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<PublishToMavenRepository>().configureEach {
    enabled = false
}
tasks.withType<PublishToMavenLocal>().configureEach {
    enabled = false
}