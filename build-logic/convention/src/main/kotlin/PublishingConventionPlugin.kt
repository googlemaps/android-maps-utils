/*
 * Copyright 2026 Google LLC
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

// buildSrc/src/main/kotlin/PublishingConventionPlugin.kt
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.*

class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            applyPlugins()
            configureKover()
            configureVanniktechPublishing()
            configureBinaryCompatibilityValidator()
        }
    }

    private fun Project.applyPlugins() {
        apply(plugin = "com.android.library")
        apply(plugin = "org.jetbrains.kotlinx.kover")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "com.vanniktech.maven.publish")
    }

    private fun Project.configureKover() {
        configure<KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        androidGeneratedClasses()
                    }
                }
            }
        }
    }

    private fun Project.configureVanniktechPublishing() {
        extensions.configure<MavenPublishBaseExtension> {
            configure(
                AndroidSingleVariantLibrary(
                    variant = "release",
                    sourcesJar = true,
                    publishJavadocJar = true
                )
            )

            publishToMavenCentral()
            if (findProperty("signing.keyId")?.toString()?.isNotBlank() == true ||
                findProperty("signing.secretKeyRingFile")?.toString()?.isNotBlank() == true ||
                findProperty("signingInMemoryKey")?.toString()?.isNotBlank() == true
            ) {
                signAllPublications()
            }

            val artifactIdName = when (project.name) {
                "maps-utils" -> "android-maps-utils"
                "library" -> "android-maps-utils-core"
                else -> "android-maps-utils-${project.name}"
            }
            coordinates(
                artifactId = artifactIdName,
            )

            pom {
                name.set("android-maps-utils")
                description.set("Handy extensions to the Google Maps Android API.")
                url.set("https://github.com/googlemaps/android-maps-utils")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:googlemaps/android-maps-utils.git")
                    developerConnection.set("scm:git@github.com:googlemaps/android-maps-utils.git")
                    url.set("https://github.com/googlemaps/android-maps-utils")
                }
                developers {
                    developer {
                        id.set("google")
                        name.set("Google LLC")
                    }
                }
                organization {
                    name.set("Google Inc")
                    url.set("http://developers.google.com/maps")
                }
            }
        }
    }

    private fun Project.configureBinaryCompatibilityValidator() {
        val ignoredProjects = setOf("demo", "visual-testing", "lint-checks", "maps-utils")
        if (name in ignoredProjects) return

        val projectName = name
        val apiFile = layout.projectDirectory.file("api/$projectName.api")
        val buildApiFile = layout.buildDirectory.file("api/$projectName.api")

        afterEvaluate {
            val bundleTask = tasks.findByName("bundleLibCompileToJarRelease") ?: return@afterEvaluate
            val classesJar = layout.buildDirectory.file(
                "intermediates/compile_library_classes_jar/release/bundleLibCompileToJarRelease/classes.jar"
            )

            val apiBuild = tasks.register<KotlinApiBuildTask>("apiBuild") {
                group = "verification"
                description = "Builds public API declaration for $projectName."
                inputJar.set(classesJar)
                outputApiFile.set(buildApiFile)
                ignoredClasses.addAll(
                    "com.google.maps.android.R",
                    "com.google.maps.android.clustering.R",
                    "com.google.maps.android.data.R",
                    "com.google.maps.android.heatmaps.R",
                    "com.google.maps.android.ui.R",
                    "com.google.maps.android.BuildConfig",
                    "com.google.maps.android.clustering.BuildConfig",
                    "com.google.maps.android.data.BuildConfig",
                    "com.google.maps.android.heatmaps.BuildConfig",
                    "com.google.maps.android.ui.BuildConfig",
                )
                dependsOn(bundleTask)
            }

            val apiDump = tasks.register<Copy>("apiDump") {
                group = "verification"
                description = "Syncs public API declarations of $projectName to the project api/ directory."
                from(apiBuild.flatMap { it.outputApiFile })
                into(apiFile.asFile.parentFile)
                dependsOn(apiBuild)
            }

            val apiCheck = tasks.register<KotlinApiCompareTask>("apiCheck") {
                group = "verification"
                description = "Checks public API declarations of $projectName against the committed api/$projectName.api."
                projectApiFile.set(apiFile)
                generatedApiFile.set(apiBuild.flatMap { it.outputApiFile })
                dependsOn(apiBuild)
            }

            tasks.findByName("check")?.dependsOn(apiCheck)

            rootProject.tasks.matching { it.name == "apiDump" }.configureEach {
                dependsOn(apiDump)
            }
            rootProject.tasks.matching { it.name == "apiCheck" }.configureEach {
                dependsOn(apiCheck)
            }
        }
    }
}
