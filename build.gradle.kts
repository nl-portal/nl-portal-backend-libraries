import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import java.net.URI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.5.31"
    val springBootVersion = "2.5.12"
    val springDependencyManagementVersion = "1.0.11.RELEASE"

    // IntelliJ
    idea

    // Apply the Kotlin JVM plugin to add support for Kotlin on the JVM.
    kotlin("jvm") version kotlinVersion apply false

    // Classes annotated with @Configuration, @Controller, @RestController, @Service or @Repository are automatically opened
    // https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.jpa") version kotlinVersion apply false
    kotlin("plugin.allopen") version kotlinVersion apply false

    // Allows to package executable jar or war archives, run Spring Boot applications,
    // and use the dependency management
    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/
    id("org.springframework.boot") version springBootVersion apply false

    // A Gradle plugin that provides Maven-like dependency management and exclusions
    // https://docs.spring.io/dependency-management-plugin/docs/current/reference/html/
    id("io.spring.dependency-management") version springDependencyManagementVersion

    // For dependency version upgrades "gradle dependencyUpdates -Drevision=release"
    id("com.github.ben-manes.versions") version "0.39.0"

    // Checkstyle
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0" apply false
    id("com.diffplug.spotless") version "5.17.0" apply false

    // Docker-compose plugin
    id("com.avast.gradle.docker-compose") version "0.14.9" apply false
}

allprojects {
    group = "com.ritense.portal"

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

    println("Enabling Spring Boot plugin in project ${project.name}...")
    apply(plugin = "org.springframework.boot")

    println("Enabling Kotlin Spring plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    println("Enabling Kotlin JPA plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    println("Enabling Kotlin All-open plugin in project ${project.name}...")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

    tasks.withType<KotlinCompile> {
        println("Configuring KotlinCompile $name in project ${project.name}...")
        kotlinOptions {
            languageVersion = "1.5"
            apiVersion = "1.5"
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xemit-jvm-type-annotations")
        }
    }

    println("Enabling Spring Boot Dependency Management in project ${project.name}...")
    apply(plugin = "io.spring.dependency-management")
    configure<DependencyManagementExtension> {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    if (project.properties.containsKey("isLib")) {
        println("Apply publishing script in project ${project.name}...")
        apply(from = "${project.rootProject.projectDir}/gradle/publishing.gradle.kts")
    }
}

println("Apply deployment script")
apply(from = "gradle/deployment.gradle")