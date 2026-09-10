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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            applyPlugins()
            configureKover()
            configureVanniktechPublishing()
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
}
