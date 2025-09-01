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
    "zgw:idtoken-authentication",
    "zgw:common-ground-authentication",
    "zgw:common-ground-authentication-test",
    "zgw:objectenapi",
    "zgw:catalogi-api",
    "zgw:openklant",
    "case",
    "data",
    "messaging",
    "form",
    "haalcentraal-hr",
    "haalcentraal2",
    "zgw:taak",
    "zgw:documenten-api",
    "zgw:berichten",
    "zgw:besluiten",
    "payment",
    "payment-direct",
    "klant",
    "klant-generiek",
    "klantcontactmomenten",
    "zgw:zaken-api",
    /*
    "form",


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

    "zgw:zaken-api",
    "zgw:objectenapi",
    "zgw:besluiten",*/
    //"zgw:openproduct",
)
