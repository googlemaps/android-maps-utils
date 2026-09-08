/*
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
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "com.google.maps.android.model"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = 23
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        androidMain.dependencies {
            // The Android actuals are typealiases to the Play Services types, so
            // this must be api(): consumers see GMS LatLng in our public API.
            api(libs.play.services.maps)
        }
    }
}
