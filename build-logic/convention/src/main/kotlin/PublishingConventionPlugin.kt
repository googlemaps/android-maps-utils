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

// buildSrc/src/main/kotlin/PublishingConventionPlugin.kt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.plugins.signing.SigningExtension
import org.gradle.api.publish.maven.*

class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {

            applyPlugins()
            configureJacoco()
            configurePublishing()
            configureSigning()
        }
    }

    private fun Project.applyPlugins() {
        apply(plugin = "com.android.library")
        apply(plugin = "com.mxalbert.gradle.jacoco-android")
        apply(plugin = "maven-publish")
        apply(plugin = "org.jetbrains.dokka")
        apply(plugin = "signing")
    }

    private fun Project.configureJacoco() {
        configure<JacocoPluginExtension> {
            toolVersion = "0.8.7"

        }

        tasks.withType<Test>().configureEach {
            extensions.configure(JacocoTaskExtension::class.java) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }

    private fun Project.configurePublishing() {
        extensions.configure<com.android.build.gradle.LibraryExtension> {
            publishing {
                singleVariant("release") {
                    withSourcesJar()
                    withJavadocJar()
                }
            }
        }
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("aar") {
                    artifactId = if (project.name == "library") {
                        "android-maps-utils"
                    } else {
                        null
                    }

                    afterEvaluate {
                        from(components["release"])
                    }
                    pom {
                        name.set(project.name)
                        description.set("Handy extensions to the Google Maps Android API.")
                        url.set("https://github.com/googlemaps/android-maps-utils")
                        scm {
                            connection.set("scm:git@github.com:googlemaps/android-maps-utils.git")
                            developerConnection.set("scm:git@github.com:googlemaps/android-maps-utils.git")
                            url.set("scm:git@github.com:googlemaps/android-maps-utils.git")
                        }
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        organization {
                            name.set("Google Inc")
                            url.set("http://developers.google.com/maps")
                        }
                        developers {
                            developer {
                                name.set("Google Inc.")
                            }
                        }
                    }
                }
            }
            repositories {
                maven {
                    val releasesRepoUrl =
                        uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    val snapshotsRepoUrl =
                        uri("https://oss.sonatype.org/content/repositories/snapshots/")
                    url = if (project.version.toString()
                            .endsWith("SNAPSHOT")
                    ) snapshotsRepoUrl else releasesRepoUrl
                    credentials {
                        username = project.findProperty("sonatypeToken") as String?
                        password = project.findProperty("sonatypeTokenPassword") as String?
                    }
                }
            }
        }
    }

    private fun Project.configureSigning() {
        configure<SigningExtension> {
            sign(extensions.getByType<PublishingExtension>().publications["aar"])
        }
    }
}
