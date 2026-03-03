import org.jreleaser.model.Active

plugins {
    id("org.jreleaser") version "1.22.0"
}

val sonatypeCentralStagingDir: String by project

val projectsExcludedFromPublish = setOf("app", "gradle", "cve-report", "license-report", "zgw", "jreleaser")

jreleaser {
    project {
        languages {
            java {
                group = "nl.nl-portal"
            }
        }
    }
    signing {
        active = Active.ALWAYS
        pgp {
            armored = true
        }
    }

    release {
        // At least one releaser must be configured, but we only want to /deploy/ to Maven Central for now
        github {
            skipTag.set(true)
            skipRelease.set(true)
        }
    }

    deploy {
        maven {
            mavenCentral {
                register("sonatype") {
                    active.set(Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    rootProject.subprojects.filter { it.name !in projectsExcludedFromPublish }.forEach { subproject ->
                        stagingRepository(
                            subproject.layout.buildDirectory.dir(sonatypeCentralStagingDir).get().asFile.path
                        )
                    }
                }
            }

            nexus2 {
                register("snapshot-deploy") {
                    active.set(Active.SNAPSHOT)
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                    applyMavenCentralRules.set(true)
                    snapshotSupported.set(true)
                    rootProject.subprojects.filter { it.name !in projectsExcludedFromPublish }.forEach { subproject ->
                        stagingRepository(
                            subproject.layout.buildDirectory.dir(sonatypeCentralStagingDir).get().asFile.path
                        )
                    }
                }
            }
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach { enabled = false }
tasks.withType<PublishToMavenLocal>().configureEach { enabled = false }
tasks.withType<Jar>().configureEach { enabled = false }
