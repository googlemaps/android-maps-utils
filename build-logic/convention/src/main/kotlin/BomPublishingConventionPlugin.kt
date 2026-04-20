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

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class BomPublishingConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            apply(plugin = "java-platform")
            apply(plugin = "com.vanniktech.maven.publish")
            
            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral()
                signAllPublications()

                coordinates(
                    artifactId = "maps-utils-bom",
                )

                pom {
                    name.set("android-maps-utils-bom")
                    description.set("BoM for android-maps-utils")
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
}
