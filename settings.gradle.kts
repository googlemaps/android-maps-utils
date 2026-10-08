/**
 * Copyright 2020 Google LLC
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("demo", "clustering", "heatmaps", "ui", "data", "lint-checks", "library", "visual-testing", "maps-utils")

listOf(
    file("../../android-maps-robolectric/main"),
    file("../android-maps-robolectric/main"),
    file("../android-maps-robolectric"),
).firstOrNull { it.resolve("settings.gradle.kts").exists() }?.let { mapsRobolectricDir ->
    includeBuild(mapsRobolectricDir) {
        dependencySubstitution {
            substitute(module("com.google.android.maps.testing:golden"))
                .using(project(":golden-testing"))
            substitute(module("com.google.android.maps.robolectric:shadows"))
                .using(project(":robolectric-testing"))
        }
    }
}
