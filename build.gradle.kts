import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
}

repositories {
    mavenCentral()
}

multiJvm {
    fun canProvideJava(javaVersion: Int): Boolean = runCatching {
        javaToolchains {
            launcherFor { languageVersion.set(JavaLanguageVersion.of(javaVersion)) }.get()
        }
    }.isSuccess

    val supportedOldJava = (7..8).map { it to canProvideJava(it) }
    val (supported, unsupported) = supportedOldJava.partition { it.second }.let { (a, b) ->
        a.map { it.first } to b.map { it.first }
    }
    require(supported.isNotEmpty()) {
        "No supported Java Version including Java 8? :/"
    }
    val javaCompileVersion: Int = supported.first()
    unsupported.forEach {
        logger.warn("Java $it is not available on this system and cannot be automatically provided")
        logger.warn("The artifact will be compiled with Java $javaCompileVersion and tested on this and newer versions")
    }
    jvmVersionForCompilation.set(javaCompileVersion)
}

dependencies {
    api(libs.gson)
    implementation(libs.jsr250)
    testImplementation(libs.junit4)
}

tasks.withType<Test> {
    failFast = true
    testLogging {
        events("passed", "skipped", "failed", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<Javadoc> {
    isFailOnError = false
}

group = "org.danilopianini"
publishOnCentral {
    repoOwner = "DanySK"
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
