/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

buildscript {
    val kotlinVersion by extra(libs.versions.kotlin.get())

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.jacoco.android)
        classpath(libs.secrets.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.dokka.gradle.plugin)
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}

tasks.register<Exec>("installAndLaunch") {
    description = "Installs and launches the demo app."
    group = "install"
    dependsOn(":demo:installDebug")
    commandLine("adb", "shell", "am", "start", "-n", "com.google.maps.android.utils.demo/.MainActivity")
}

allprojects {
    group = "com.google.maps.android"
    // {x-release-please-start-version}
    version = "4.0.0"
    // {x-release-please-end}
}