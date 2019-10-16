import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    buildSrcVersions
    id("org.danilopianini.git-sensitive-semantic-versioning") version Versions.org_danilopianini_git_sensitive_semantic_versioning_gradle_plugin
    `java-library`
    id("org.jlleitschuh.gradle.ktlint") version Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central") version Versions.org_danilopianini_publish_on_central_gradle_plugin
}

repositories {
    mavenCentral()
}

gitSemVer {
    version = computeGitSemVer()
}

val java6Home: String by project //= System.getenv()["JAVA6_HOME"]

    ?: throw IllegalStateException("""
        The required environment variable JAVA6_HOME is not defined.
        gson-extras is cross-compiled against Java 6, and requires a valid JDK6 installation to complete the build"""
        .trimIndent()
    )
val java6HomeDirectory = File(java6Home).takeIf { it.exists() and it.isDirectory }
    ?: throw IllegalStateException("JAVA6_HOME is set to $java6Home,  but it is not a valid path.")

java {
    sourceCompatibility = JavaVersion.toVersion("1.6")
}

val javaExecutablesPath = File(java6Home, "bin")
fun javaExecutable(execName: String): String {
    val executable = File(javaExecutablesPath, execName)
    require(executable.exists()) { "There is no ${execName} executable in ${javaExecutablesPath}" }
    return executable.toString()
}
tasks.withType<JavaCompile>().configureEach {
    options.apply {
        isFork = true
        forkOptions.javaHome = file(java6Home)
    }
}

dependencies {
    api("com.google.code.gson:gson:2.8.6")
    implementation("javax.annotation:jsr250-api:1.0")
    testImplementation("junit:junit:[4.12, 5[")
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