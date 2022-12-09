apply(plugin = "java")
apply(plugin = "maven-publish")

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(project.the<SourceSetContainer>()["main"].allSource)
}

configure<PublishingExtension> {
    repositories {
        maven {
            val repoUser = System.getenv("MVN_REPO_USR")
            if (!repoUser.isNullOrBlank()) {
                credentials {
                    username = repoUser
                    password = System.getenv("MVN_REPO_PWD")
                }
            }

            url = if (project.version.toString().endsWith("-SNAPSHOT", true)) {
                uri(System.getenv("MVN_REPO_SNAPSHOT_URL") ?: "http://localhost")
            } else {
                uri(System.getenv("MVN_REPO_RELEASES_URL") ?: "http://localhost")
            }
        }
    }

    publications {
        publications {
            register<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(sourcesJar.get())
            }
        }
    }
}