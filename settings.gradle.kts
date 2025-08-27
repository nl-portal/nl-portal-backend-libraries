rootProject.name = "nl-portal-backend-libraries"

pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val benManesVersionsVersion: String by settings
    val spotlessVersion: String by settings
    val gradleDockerComposeVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.spring") version kotlinVersion apply false
        kotlin("plugin.jpa") version kotlinVersion apply false
        kotlin("plugin.allopen") version kotlinVersion apply false

        id("org.springframework.boot") version springBootVersion apply false
        id("io.spring.dependency-management") version springDependencyManagementVersion apply false
        id("com.github.ben-manes.versions") version benManesVersionsVersion apply false
        id("com.diffplug.spotless") version spotlessVersion apply false
        id("com.avast.gradle.docker-compose") version gradleDockerComposeVersion apply false
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    "app",
    "core",
    "gradle:cve-report",
    "gradle:license-report",
    "graphql",
    "portal-authentication",
    "zgw:common-ground-authentication",
    "zgw:common-ground-authentication-test",
    "zgw:openklant",
    /*
    "case",
    "data",
    "form",
    "haalcentraal-hr",
    "haalcentraal2",
    "klant",
    "klant-generiek",
    "klantcontactmomenten",
    "messaging",
    "product",
    "portal-authentication",
    "payment",
    "payment-direct",
    "zgw:berichten",
    "zgw:catalogi-api",
    "zgw:common-ground-authentication",
    "zgw:common-ground-authentication-test",
    "zgw:documenten-api",
    "zgw:idtoken-authentication",
    "zgw:taak",
    "zgw:zaken-api",
    "zgw:objectenapi",
    "zgw:besluiten",*/
    //"zgw:openproduct",
)
