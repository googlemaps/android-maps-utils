import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Copyright 2024 Google Inc.
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
    namespace = "com.google.maps.android"
    sourceSets["main"].java.srcDir("build/generated/source/artifactId")
}

dependencies {
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
    implementation(libs.androidx.startup.runtime)

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

// START: Attribution ID Generation Logic

// 1. Define the attribution string format.
//    It uses the 'version' property inherited from the root build.gradle.kts.
//    Customize the prefix to match your library's identifier.
val attributionId = "gmp_git_androidmapsutils_v$version"

// 2. Register the custom Gradle task.
val generateArtifactIdFile = tasks.register("generateArtifactIdFile") {
    description = "Generates an AttributionId object from the project version."
    group = "build"

    // 3. Define the output directory and file.
    val outputDir = layout.buildDirectory.dir("generated/source/artifactId")
    val packageName = "com.google.maps.android.utils.meta" // <-- Customize your package name
    val packagePath = packageName.replace('.', '/')
    val outputFile = outputDir.get().file("$packagePath/ArtifactId.kt").asFile

    // 4. Declare the output file so Gradle understands task dependencies.
    outputs.file(outputFile)

    // 5. Define the action that writes the file.
    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            // This is the content of the generated Kotlin file.
            // It uses Kotlin's trimIndent() for clean formatting.
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
        println("Generated attribution ID file at: ${outputFile.absolutePath}")
    }
}

// 6. Hook the custom task into the build lifecycle.
//    This ensures your file is generated before the Kotlin compiler needs it.
tasks.named("preBuild") {
    dependsOn(generateArtifactIdFile)
}

// END: Attribution ID Generation Logic
