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
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    // Prototype publishing: KMP auto-creates multiplatform publications, enabling
    // publishToMavenLocal so android-maps-compose can consume this via -PuseMavenLocal=true.
    id("maven-publish")
}

// NOTE (KMP prototype): see clustering/build.gradle.kts — release publishing (vanniktech),
// lint-checks and the amu_ resourcePrefix still need KMP-aware re-wiring.

abstract class GenerateArtifactIdTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val version: Property<String>

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        val packageName = "com.google.maps.android.utils.meta"
        val packagePath = packageName.replace('.', '/')
        val outputFile = File(dir, "$packagePath/ArtifactId.kt")
        outputFile.parentFile.mkdirs()
        val attributionId = "gmp_git_androidmapsutils_v${version.get()}"
        outputFile.writeText(
            """
            package $packageName

            /**
             * Automatically generated object containing the library's attribution ID.
             * This is used to track library usage for analytics.
             */
            public object AttributionId {
                public const val VALUE: String = "$attributionId"
            }
            """.trimIndent()
        )
    }
}

val generateArtifactIdFile = tasks.register<GenerateArtifactIdTask>("generateArtifactIdFile") {
    outputDir.set(layout.buildDirectory.dir("generated/source/artifactId"))
    version.set(project.version.toString())
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "com.google.maps.android"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = 23

        withHostTestBuilder {
        }.configure {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            api(project(":maps-model"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain {
            kotlin.srcDir(generateArtifactIdFile)
        }
        androidMain.dependencies {
            api(libs.play.services.maps)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.appcompat)
            implementation(libs.core.ktx)
            implementation(libs.startup.runtime)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.robolectric)
            implementation(libs.kxml2)
            implementation(libs.mockk)
            implementation(libs.androidx.test.core)
            implementation(libs.truth)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// Publish under the repo's public artifactId scheme so these coordinates conflict-resolve
// against the AARs already on Maven Central instead of duplicating their classes.
// This module's public artifactId is android-maps-utils-core.
publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = artifactId.replace(project.name, "android-maps-utils-core")
    }
}
