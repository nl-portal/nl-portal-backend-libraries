import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import io.spring.gradle.dependencymanagement.org.codehaus.plexus.interpolation.os.Os.FAMILY_MAC
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI
import kotlin.io.encoding.Base64.Default.decode
import kotlin.io.encoding.ExperimentalEncodingApi

val sonatypeCentralStagingDir = "sonatypeCentralStaging"
val projectsExcludedFromPublish = setOf("app", "gradle", "cve-report", "license-report", "zgw")

plugins {
    java

    // IntelliJ
    idea

    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm")

    // Classes annotated with @Configuration, @Controller, @RestController, @Service or @Repository are automatically opened
    // https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support

    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")

    // Allows to package executable jar or war archives, run Spring Boot applications,
    // and use the dependency management
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    id("org.springframework.boot")

    // A Gradle plugin that provides Maven-like dependency management and exclusions
    // https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/
    id("io.spring.dependency-management")

    // For dependency version upgrades "gradle dependencyUpdates -Drevision=release"
    id("com.github.ben-manes.versions")

    // Checkstyle
    id("com.diffplug.spotless")

    // Docker-compose plugin
    id("com.avast.gradle.docker-compose")

    id("com.github.jk1.dependency-license-report") version "2.9"

    id("org.owasp.dependencycheck") version "12.1.6"

    id("org.jreleaser") version "1.19.0"

    `maven-publish`
    signing
}

allprojects {
    repositories {
        mavenCentral()
        maven(URI("https://repository.jboss.org/nexus/content/repositories/releases"))
        maven(URI("https://oss.sonatype.org/content/repositories/releases"))
        maven(URI("https://app.camunda.com/nexus/content/groups/public"))
        maven(URI("https://s01.oss.sonatype.org/content/groups/staging/"))
        maven(URI("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
}

jreleaser {
    project {
        java {
            groupId.set("nl.nl-portal")
        }
    }
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
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
                    active.set(org.jreleaser.model.Active.RELEASE)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    subprojects.filter { it.name !in projectsExcludedFromPublish }.forEach { project ->
                        stagingRepository(project.layout.buildDirectory.dir(sonatypeCentralStagingDir).get().asFile.path)
                    }
                }
            }

            nexus2 {
                register("snapshot-deploy") {
                    active.set(org.jreleaser.model.Active.SNAPSHOT)
                    snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                    applyMavenCentralRules.set(true)
                    snapshotSupported.set(true)
                    closeRepository.set(true)
                    releaseRepository.set(true)
                    subprojects.filter { it.name !in projectsExcludedFromPublish }.forEach { project ->
                        stagingRepository(project.layout.buildDirectory.dir(sonatypeCentralStagingDir).get().asFile.path)
                    }
                }
            }
        }
    }
}

subprojects {
    println("Enabling com.avast.gradle.docker-compose plugin in project ${project.name}...")
    apply(plugin = "com.avast.gradle.docker-compose")

    println("Enabling com.diffplug.spotless plugin in project ${project.name}...")
    apply(plugin = "com.diffplug.spotless")

    apply(plugin = "java")

    apply(plugin = "maven-publish")

    var signingConfigSet = false
    if (System.getenv("SIGNING_KEY") != null &&
        System.getenv("SIGNING_KEY_PASSWORD") != null
    ) {
        signingConfigSet = true
        apply(plugin = "signing")
    }

    if (project.properties.containsKey("isLib") || project.properties.containsKey("isApp")) {
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            kotlin {
                ktlint()
                // by default the target is every '.kt' and '.kts` file in the java sourcesets
                licenseHeaderFile("licenseHeaderFile.template") // or licenseHeaderFile.template
            }
            kotlinGradle {
                target("*.gradle.kts") // default target for kotlinGradle
            }
        }
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    if (!(project.path.contains("gradle"))) {
        println("Enabling Spring Boot plugin in project ${project.name}...")
        apply(plugin = "org.springframework.boot")
    }

    println("Enabling Kotlin Spring plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    println("Enabling Kotlin JPA plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    println("Enabling Kotlin All-open plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
            freeCompilerArgs.add("-Xemit-jvm-type-annotations")
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }

    println("Enabling Spring Boot Dependency Management in project ${project.name}...")
    apply(plugin = "io.spring.dependency-management")
    configure<DependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) {
                bomProperty("graphql-java.version", Versions.graphqlJava)
            }
        }
        dependencies {
            // JReleaser 1.19.0 still expects the JGit 6.x API; Boot 3.5 BOM promotes JGit 7.x
            dependency("org.eclipse.jgit:org.eclipse.jgit:6.10.1.202505221210-r")
            dependency("org.eclipse.jgit:org.eclipse.jgit.gpg.bc:6.10.1.202505221210-r")
        }
    }

    publishing {
        repositories {
            maven {
                name = "Sonatype"
                url = uri(project.layout.buildDirectory.dir(sonatypeCentralStagingDir).get().asFile.path)
            }
        }

        publications {
            register<MavenPublication>("default") {
                groupId = "nl.nl-portal"
                pom {
                    url = "https://github.com/nl-portal/nl-portal-backend-libraries"
                    licenses {
                        license {
                            name = "Licensed under EUPL, Version 1.2 (the \"License\");"
                            url = "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12"
                        }
                    }
                    scm {
                        connection = "git@github.com:nl-portal/nl-portal-backend-libraries.git"
                        developerConnection = "git@github.com:nl-portal/nl-portal-backend-libraries.git"
                        url = "https://github.com/nl-portal/nl-portal-backend-libraries"
                    }
                }
                from(components["java"])
            }
        }
    }

    apply(from = "${rootProject.projectDir}/gradle/testing.gradle.kts")

    if (Os.isFamily(FAMILY_MAC)) {
        println("Configure docker compose for macOs")
        dockerCompose {
            executable = "/usr/local/bin/docker-compose"
            dockerExecutable = "/usr/local/bin/docker"
        }
    }
}

tasks.register<HtmlDependencyReportTask>("htmlDependencyReport")

tasks.named<HtmlDependencyReportTask>("htmlDependencyReport") {
    projects = project.allprojects
    reports.html.outputLocation = file("build/reports/project/dependencies")
}

tasks.bootJar {
    enabled = false
}

@OptIn(ExperimentalEncodingApi::class)
fun getSigningKey(signingKeyBase64: String): ByteArray {
    return decode(signingKeyBase64.subSequence(0, signingKeyBase64.length))
}

tasks.withType<PublishToMavenRepository> {
    enabled = false
}
tasks.withType<PublishToMavenLocal> {
    enabled = false
}
