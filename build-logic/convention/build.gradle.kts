plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}


dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.org.jacoco.core)
}

gradlePlugin {
    plugins {
        register("publishingConventionPlugin") {
            id = "android.maps.utils.PublishingConventionPlugin"
            implementationClass = "PublishingConventionPlugin"
        }
    }
}