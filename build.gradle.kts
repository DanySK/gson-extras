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

java {
    sourceCompatibility = JavaVersion.toVersion("1.6")
}

val java6Home: String? by project
tasks.withType<JavaCompile>().configureEach {
    if (java6Home == null) {
        doFirst {
            throw IllegalStateException("java6Home is not set, Java compilation disabled.")
        }
    } else {
        doFirst {
            val java6HomeDirectory = File(java6Home).takeIf { it.exists() and it.isDirectory }
                    ?: throw IllegalStateException("java6Home is set to $java6Home,  but it is not a valid path.")
            val javaExecutablesPath = File(java6Home, "bin")
            fun javaExecutable(execName: String): String {
                val executable = File(javaExecutablesPath, execName)
                require(executable.exists()) { "There is no $execName executable in $javaExecutablesPath" }
                return executable.toString()
            }
        }
        options.apply {
            isFork = true
            forkOptions.javaHome = file(java6Home ?: throw IllegalStateException("Cannot happen."))
        }
    }
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
    projectDescription.set("Extra goodies for Gson, available on Google's Github repository, made available on Central")
    projectLongName.set("Gson Extras")
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
