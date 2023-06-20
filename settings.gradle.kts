rootProject.name = "nl-portal-backend-libraries"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val benManesVersionsVersion: String by settings
    val ktlintVersion: String by settings
    val spotlessVersion: String by settings
    val gradleDockerComposeVersion: String by settings
    val dokkaVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.spring") version kotlinVersion apply false
        kotlin("plugin.jpa") version kotlinVersion apply false
        kotlin("plugin.allopen") version kotlinVersion apply false

        id("org.springframework.boot") version springBootVersion apply false
        id("io.spring.dependency-management") version springDependencyManagementVersion apply false
        id("com.github.ben-manes.versions") version benManesVersionsVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion apply false
        id("com.diffplug.spotless") version spotlessVersion apply false
        id("com.avast.gradle.docker-compose") version gradleDockerComposeVersion apply false
        id("org.jetbrains.dokka") version dokkaVersion apply false
    }
}

include(
    "app:portal",
    "app:gzac",
    "aws:s3",
    "case",
    "common-ground-authentication",
    "common-ground-authentication-test",
    "core",
    "data",
    "form",
    "form-flow",
    "gradle:license-report",
    "graphql",
    "gzac",
    "gzac:objects-api",
    "haalcentraal:haalcentraal-all",
    "haalcentraal:haalcentraal-brp",
    "haalcentraal:haalcentraal-hr",
    "klant",
    "messaging",
    "product",
    "task",
    "zaak"
)