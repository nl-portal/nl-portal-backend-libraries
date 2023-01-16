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

pluginManager.withPlugin("maven-publish") {
    configure<PublishingExtension> {
        publications {
            withType(MavenPublication::class.java) {
                pom {
                    getName().set("Haalcentraal container module")
                    getDescription().set("This module provides haalcentraal integration, allowing you to query for common data for Dutch citizen in a generic way.")
                    developers {
                        developer {
                            getId().set("team-nl-portal")
                            getName().set("Team NL Portal")
                            getEmail().set("team-nl-portal@ritense.com")
                        }
                    }
                }
            }
        }
    }
}