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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(
    "app",
    "case",
    "core",
    "data",
    "form",
    "gradle:cve-report",
    "gradle:license-report",
    "graphql",
    "haalcentraal:haalcentraal-all",
    "haalcentraal:haalcentraal-brp",
    "haalcentraal:haalcentraal-hr",
    "klant",
    "klant-generiek",
    "klantcontactmomenten",
    "messaging",
    "product",
    "portal-authentication",
    "payment",
    "zgw:berichten",
    "zgw:catalogi-api",
    "zgw:common-ground-authentication",
    "zgw:common-ground-authentication-test",
    "zgw:documenten-api",
    "zgw:idtoken-authentication",
    "zgw:taak",
    "zgw:zaken-api",
    "zgw:objectenapi",
    "zgw:besluiten",
)