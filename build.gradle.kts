import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI
import kotlin.io.encoding.Base64.Default.decode
import kotlin.io.encoding.ExperimentalEncodingApi

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
    id("org.jlleitschuh.gradle.ktlint")
    id("com.diffplug.spotless")

    // Docker-compose plugin
    id("com.avast.gradle.docker-compose")

    id("com.github.jk1.dependency-license-report") version "2.9"

    id("org.jetbrains.dokka")

    id("org.owasp.dependencycheck") version "10.0.3"

    `maven-publish`
    `signing`
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

subprojects {
    println("Enabling com.avast.gradle.docker-compose plugin in project ${project.name}...")
    apply(plugin = "com.avast.gradle.docker-compose")

    println("Enabling org.jlleitschuh.gradle.ktlint plugin in project ${project.name}...")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    println("Enabling com.diffplug.spotless plugin in project ${project.name}...")
    apply(plugin = "com.diffplug.spotless")

    apply(plugin = "org.jetbrains.dokka")

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
        }
    }

    println("Enabling Spring Boot Dependency Management in project ${project.name}...")
    apply(plugin = "io.spring.dependency-management")
    configure<DependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) {
                bomProperty("graphql-java.version", ApiVersions.graphqlJava)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/nl-portal/nl-portal-backend-libraries")
                credentials {
                    username = System.getenv("USER")
                    password = System.getenv("TOKEN")
                }
            }
            maven {
                name = "Sonatype"
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_TOKEN")
                }

                var stagingRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

                if (version.toString().matches("^(\\d+\\.)?(\\d+\\.)?(\\d+)\$".toRegex())) {
                    url = stagingRepoUrl
                }
            }
            maven {
                name = "SonatypeSnapshot"
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_TOKEN")
                }

                var snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                if (version.toString().endsWith("SNAPSHOT")) {
                    url = snapshotsRepoUrl
                }
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

    if (signingConfigSet) {
        signing {
            val signingKeyBase64: String? = System.getenv("SIGNING_KEY")
            val signingKeyBytes: ByteArray = getSigningKey(signingKeyBase64!!)
            val signingKey: String = signingKeyBytes.toString(Charsets.UTF_8)
            val signingKeyPassword: String? = System.getenv("SIGNING_KEY_PASSWORD")

            useInMemoryPgpKeys(signingKey, signingKeyPassword)
            sign(publishing.publications["default"])
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
// println("Apply deployment script")
// apply(from = "gradle/deployment.gradle")