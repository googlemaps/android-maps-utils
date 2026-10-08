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

import com.android.build.api.dsl.Lint
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention for the Kotlin Multiplatform library modules (Android plus iOS).
 *
 * Mirrors [PublishingConventionPlugin] for the Android-only modules: explicit API mode, Kover,
 * Dokka and Maven Central publishing under the same artifactIds and POM. Each module still
 * configures its own `androidLibrary { }` target (namespace, SDK levels) and source sets.
 *
 * The binary compatibility validator only checks the iOS klib ABI of these modules
 * (`api/<module>.klib.api`). The Android API is checked here against the same
 * `api/<module>.api` file the module had before it became multiplatform, so moving to KMP
 * cannot change the Android API without the check failing.
 */
class KmpPublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            apply(plugin = "org.jetbrains.kotlin.multiplatform")
            apply(plugin = "com.android.kotlin.multiplatform.library")
            // Multiplatform Android libraries only get lint tasks from the standalone lint plugin.
            apply(plugin = "com.android.lint")
            apply(plugin = "org.jetbrains.kotlinx.kover")
            apply(plugin = "org.jetbrains.dokka")
            apply(plugin = "com.vanniktech.maven.publish")

            configure<KotlinMultiplatformExtension> {
                explicitApi()
                jvmToolchain(17)
                iosArm64()
                iosSimulatorArm64()
                iosX64()
            }

            configure<Lint> {
                sarifOutput = layout.buildDirectory.file("reports/lint-results.sarif").get().asFile
            }

            configure<KoverProjectExtension> {
                // CI and the coverage history read koverXmlReportDebug / reportDebug.xml, which
                // the Android-only modules produce. Expose the KMP Android coverage the same way.
                currentProject {
                    createVariant("debug") {
                        add("android")
                    }
                }
                reports {
                    filters {
                        excludes {
                            androidGeneratedClasses()
                        }
                    }
                }
            }

            configure<MavenPublishBaseExtension> {
                configure(
                    KotlinMultiplatform(
                        javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                        sourcesJar = SourcesJar.Sources(),
                    )
                )
                configureMapsUtilsPublishing(project)
            }

            configureAndroidApiValidation()
        }
    }

    private fun Project.configureAndroidApiValidation() {
        val projectName = name
        val apiFile = layout.projectDirectory.file("api/$projectName.api")
        val buildApiFile = layout.buildDirectory.file("api/android/$projectName.api")

        afterEvaluate {
            val bundleTask = tasks.findByName("bundleAndroidMainClassesToCompileJar") ?: return@afterEvaluate
            val classesJar = layout.buildDirectory.file(
                "intermediates/compile_library_classes_jar/androidMain/bundleAndroidMainClassesToCompileJar/classes.jar"
            )

            val androidApiBuild = tasks.register<KotlinApiBuildTask>("androidApiBuild") {
                group = "verification"
                description = "Builds the public Android API declaration for $projectName."
                inputJar.set(classesJar)
                outputApiFile.set(buildApiFile)
                ignoredClasses.addAll(
                    "com.google.maps.android.R",
                    "com.google.maps.android.BuildConfig",
                    "com.google.maps.android.$projectName.R",
                    "com.google.maps.android.$projectName.BuildConfig",
                )
                dependsOn(bundleTask)
            }

            val androidApiDump = tasks.register<Copy>("androidApiDump") {
                group = "verification"
                description = "Syncs the public Android API declaration of $projectName to api/$projectName.api."
                from(androidApiBuild.flatMap { it.outputApiFile })
                into(apiFile.asFile.parentFile)
                dependsOn(androidApiBuild)
            }

            val androidApiCheck = tasks.register<KotlinApiCompareTask>("androidApiCheck") {
                group = "verification"
                description = "Checks the public Android API of $projectName against the committed api/$projectName.api."
                projectApiFile.set(apiFile)
                generatedApiFile.set(androidApiBuild.flatMap { it.outputApiFile })
                dependsOn(androidApiBuild)
            }

            tasks.named("apiDump") { dependsOn(androidApiDump) }
            tasks.named("apiCheck") { dependsOn(androidApiCheck) }
            tasks.findByName("check")?.dependsOn(androidApiCheck)
        }
    }
}
