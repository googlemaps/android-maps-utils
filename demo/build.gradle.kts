/**
 * Copyright 2026 Google LLC
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

import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
    alias(libs.plugins.compose.compiler)
}

val secretsFile = rootProject.file("secrets.properties")
if (!secretsFile.exists()) {
    val taskNames = gradle.startParameter.taskNames
    // 1. Allow IDE Sync (which runs with empty tasks)
    if (taskNames.isEmpty()) {
        println("⚠️ Warning: secrets.properties missing. IDE sync will succeed, but builds will fail.")
    } else {
        // 2. Normalize task names to handle ":demo:assembleDebug" -> "assembleDebug"
        val simpleTaskNames = taskNames.map { it.substringAfterLast(":") }

        // 3. Identify if the user is explicitly asking for an app build
        val isExplicitBuild = simpleTaskNames.any {
            it == "build" ||
                    it.startsWith("assemble") ||
                    it.startsWith("install") ||
                    it.startsWith("bundle")
        }

        // 4. Identify if the user is running tests/lint
        //    (We check for "Test" to allow tasks like 'assembleAndroidTest' to proceed if desired)
        val isTestOrLint = simpleTaskNames.any {
            val lower = it.lowercase()
            lower.contains("test") || lower.contains("lint")
        }

        // 5. Fail ONLY if it's a build task that isn't also a test task
        if (isExplicitBuild && !isTestOrLint) {
            throw GradleException("Build Blocked: 'secrets.properties' is missing.\n\n" +
                    "🛑 To build the demo app, you must create the 'secrets.properties' file in the root directory:\n\n" +
                    "  MAPS_API_KEY=AIza...\n" +
                    "  PLACES_API_KEY=AIza...  # Only needed for certain demos (e.g., HeatmapsPlacesDemoActivity.java)\n" +
                    "  MAP_ID=...\n\n" +
                    "Or run unit tests only: ./gradlew test")
        } else {
            println("⚠️ Warning: secrets.properties missing. Building/Running the demo app will fail, but testing is allowed.")
        }
    }
}

android {
    lint {
        sarifOutput = layout.buildDirectory.file("reports/lint-results.sarif").get().asFile
    }

    defaultConfig {
        compileSdk = libs.versions.compileSdk.get().toInt()
        applicationId = "com.google.maps.android.utils.demo"
        minSdk = libs.versions.minimumSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        jvmToolchain(17)
    }

    namespace = "com.google.maps.android.utils.demo"
}

configurations.all {
    resolutionStrategy {
        force(libs.kotlinx.coroutines.core)
        force(libs.kotlinx.coroutines.android)
        force(libs.kotlinx.serialization.json)
    }
}

// [START maps_android_utils_install_snippet]
dependencies {
    // [START_EXCLUDE silent]
    implementation(project(":clustering"))
    implementation(project(":heatmaps"))
    implementation(project(":ui"))
    implementation(project(":data"))
    implementation(project(":onion"))

    implementation(libs.appcompat)
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.material)
    implementation(libs.core.ktx)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.truth)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso-core)
    androidTestImplementation(libs.truth)

    implementation(project(":visual-testing"))
    implementation(libs.uiautomator)

    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons.core)
    debugImplementation(libs.ui.tooling)
    // [END_EXCLUDE]
}
// [END maps_android_utils_install_snippet]

secrets {
    // To add your Maps API key to this project:
    // 1. Create a file ./secrets.properties
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    defaultPropertiesFileName ="local.defaults.properties"
    propertiesFileName = "secrets.properties"
}
