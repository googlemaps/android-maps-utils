import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    id("kotlin-android")
    id("org.jetbrains.dokka")
    id("android.maps.utils.PublishingConventionPlugin")
}

android {
    lint {
        sarifOutput = layout.buildDirectory.file("reports/lint-results.sarif").get().asFile
    }
    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = 21
        testOptions.targetSdk = libs.versions.targetSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    resourcePrefix = "amu_"

    installation {
        timeOutInMs = 10 * 60 * 1000 // 10 minutes
        installOptions("-d", "-t")
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        jvmToolchain(17)
    }

    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
    namespace = "com.google.maps.android.heatmaps"
}

dependencies {
    implementation(project(":clustering"))
    implementation(project(":data"))
    api(libs.play.services.maps)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    lintPublish(project(":lint-checks"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kxml2)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.truth)
    implementation(libs.kotlin.stdlib.jdk8)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockito.core)
}

tasks.register("instrumentTest") {
    dependsOn("connectedCheck")
}

if (System.getenv("JITPACK") != null) {
    apply(plugin = "maven")
}
