import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val secretsFile = rootProject.file("secrets.properties")
if (!secretsFile.exists()) {
    val requestedTasks = gradle.startParameter.taskNames
    if (requestedTasks.isEmpty()) {
        // It's likely an IDE sync if no tasks are specified, so just issue a warning.
        println("Warning: secrets.properties not found. Gradle sync may succeed, but building/running the demo app will fail.")
    } else {
        // Tasks that are allowed to run without a secrets.properties file.
        val buildTaskKeywords = listOf("build", "install", "assemble")
        val isBuildTask = requestedTasks.any { task ->
            buildTaskKeywords.any { keyword ->
                task.contains(keyword, ignoreCase = true)
            }
        }

        val testTaskKeywords = listOf("test", "report", "lint")
        val isTestTask = requestedTasks.any { task ->
            testTaskKeywords.any { keyword ->
                task.contains(keyword, ignoreCase = true)
            }
        }

        if (isBuildTask && !isTestTask) {
            throw GradleException("secrets.properties file not found. Please create a 'secrets.properties' file in the root project directory with the following content:\n" +
                    "\n" +
                    "MAPS_API_KEY=<YOUR_API_KEY>\n" +
                    "PLACES_API_KEY=<YOUR_API_KEY>  # Only needed for certain demos (e.g., HeatmapsPlacesDemoActivity.java)\n" +
                    "MAP_ID=<YOUR_MAP_ID>\n")
        } else {
            // For exempt tasks, we don't need the secrets, but we can print a warning.
            println("Warning: secrets.properties not found. You can run tests and lint, but you won't be able to build or run the demo app.")
        }
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
