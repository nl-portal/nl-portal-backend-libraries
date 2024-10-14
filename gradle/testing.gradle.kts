/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

tasks.register<Test>("integrationTest") {
    group = "verification"
    description =
        """
        Composes docker containers and runs Tests tagged with "integration".
        NB! Project root must contain a docker compose file with the following name: docker-compose-override.yml
        """.trimIndent()
    useJUnitPlatform {
        includeTags("integration")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.named("check") {
    dependsOn(tasks.getByName("integrationTest"))
}