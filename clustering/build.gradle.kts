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
plugins {
    id("android.maps.utils.KmpPublishingConventionPlugin")
}

kotlin {
    androidLibrary {
        namespace = "com.google.maps.android.clustering"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minimumSdk.get().toInt()

        withHostTestBuilder {
        }.configure {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":maps-model"))
            // androidx.collection is multiplatform; LongSparseArray/LruCache work in common code
            implementation(libs.androidx.collection)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(project(":ui"))
            implementation(project(":library"))
            implementation(project(":data"))
            api(libs.play.services.maps)
            api(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.appcompat)
            implementation(libs.core.ktx)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.kxml2)
            implementation(libs.mockk)
            implementation(libs.kotlin.test)
            implementation(libs.truth)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockito.kotlin)
        }
    }
}

dependencies {
    lintPublish(project(":lint-checks"))
}
