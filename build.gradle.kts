import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning")
    `java-library`
    id("org.jlleitschuh.gradle.ktlint")
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central")
}

repositories {
    mavenCentral()
}

gitSemVer {
    version = computeGitSemVer()
}

dependencies {
    api("com.google.code.gson:gson:_")
    implementation("javax.annotation:jsr250-api:_")
    testImplementation("junit:junit:_")
}

tasks.withType<Test> {
    failFast = true
    testLogging {
        events("passed", "skipped", "failed", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

group = "org.danilopianini"
publishOnCentral {
    projectDescription = "Extra goodies for Gson, available on Google's Github repository, made available on Central"
    projectLongName = "Gson Extras"
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@unibo.it")
                        url.set("http://www.danilopianini.org/")
                    }
                    developer {
                        name.set("Matteo Francia")
                        email.set("matteo.francia2@studio.unibo.it")
                        url.set("http://www.danilopianini.org/")
                    }
                }
            }
        }
    }
}

if (System.getenv("CI") == true.toString()) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
