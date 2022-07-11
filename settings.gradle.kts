rootProject.name = "gson-extras"
enableFeaturePreview("VERSION_CATALOGS")

plugins {
    id("com.gradle.enterprise") version "3.10.3"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}
