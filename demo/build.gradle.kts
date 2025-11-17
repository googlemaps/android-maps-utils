import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Only require secrets for build/install tasks
val requestedTasks = gradle.startParameter.taskNames
val isBuildTask = requestedTasks.any { task ->
    // Check for generic tasks that imply building everything, including demo
    val isGenericBuild = task.equals("build", ignoreCase = true) || task.equals("assemble", ignoreCase = true)

    // Check for tasks that specifically target the demo project, e.g. `./gradlew :demo:assembleDebug`
    val isDemoTargeted = task.contains(":demo", ignoreCase = true) &&
        (
            task.endsWith("assemble", ignoreCase = true) ||
            task.endsWith("install", ignoreCase = true) ||
            task.endsWith("run", ignoreCase = true) ||
            task.endsWith("bundle", ignoreCase = true) ||
            task.endsWith("build", ignoreCase = true)
        )
    isGenericBuild || isDemoTargeted
}

val secretsFile = rootProject.file("secrets.properties")
if (!secretsFile.exists()) {
    if (isBuildTask) {
        throw GradleException(
            "secrets.properties file not found. This file is required to build the demo application. " +
                    "Please create one in the root project directory with your MAPS_API_KEY."
        )
    } else {
        // For other tasks like 'test' or 'clean', we don't need the secrets,
        // but we can print a warning.
        println("Warning: secrets.properties not found. You can run tests and lint, but you won't be able to build or run the demo app.")
    }
}




/**
 * Copyright 2025 Google LLC
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
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlin-android")
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
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        jvmToolchain(17)
    }

    namespace = "com.google.maps.android.utils.demo"
}

// [START maps_android_utils_install_snippet]
dependencies {
    // [START_EXCLUDE silent]
    implementation(project(":clustering"))
    implementation(project(":heatmaps"))
    implementation(project(":ui"))
    implementation(project(":data"))

    implementation(libs.appcompat)
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.material)
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
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
