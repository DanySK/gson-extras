import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
}

ext {
    set(
        "org.gradle.java.installations.paths",
        file("${System.getProperty("user.home")}/.jab/jdk")
            .listFiles()
            ?.filter { it.isDirectory }
            ?.onEach { println("files in ${it.path}: ${it.listFiles().toList()}") }
            ?.joinToString(separator = ",")
            ?: ""
    )
}

println(property("org.gradle.java.installations.paths"))

repositories {
    mavenCentral()
}

multiJvm {
    fun canProvideJava(javaVersion: Int): Boolean = runCatching {
        javaToolchains {
            launcherFor { languageVersion.set(JavaLanguageVersion.of(6)) }.get()
        }
    }.isSuccess

    val supportedOldJava = (6..8).map { it to canProvideJava(it) }
    val (supported, unsupported) = supportedOldJava.partition { it.second }.let { (a, b) ->
        a.map { it.first } to b.map { it.first }
    }
    val javaCompileVersion: Int = supported.firstOrNull() ?: 8
    unsupported.forEach {
        logger.warn("Java $it is not available on this system and cannot be automatically provided")
        logger.warn("The artifact will be compiled with Java $javaCompileVersion tested on this an newer versions")
    }
    jvmVersionForCompilation.set(javaCompileVersion)
    val jvmTestVersions = supported.filter { it > 6 }
    testByDefaultWith(
        supportedLtsVersionsAndLatest.map { versions ->
            (versions + jvmTestVersions).toSet()
        }
    )
    afterEvaluate {
        tasks.named("test") {
            require(this is Test)
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(jvmTestVersions.firstOrNull() ?: 8))
                }
            )
        }
    }
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
