rootProject.name = "gson-extras"

plugins {
    id("com.gradle.enterprise") version "3.13.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}
