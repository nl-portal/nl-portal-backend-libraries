import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

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

    id("com.github.jk1.dependency-license-report") version "2.1"

    id("org.jetbrains.dokka")
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

    tasks.withType<KotlinCompile> {
        println("Configuring KotlinCompile $name in project ${project.name}...")
        kotlinOptions {
            languageVersion = "1.7"
            apiVersion = "1.7"
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xemit-jvm-type-annotations")
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

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/nl-portal/nl-portal-backend-libraries")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {

            register<MavenPublication>("default") {
                groupId = "nl.nl-portal"
                // from(components["java"])
            }
        }
    }
}

tasks.bootJar {
    enabled = false
}

println("Apply deployment script")
apply(from = "gradle/deployment.gradle")