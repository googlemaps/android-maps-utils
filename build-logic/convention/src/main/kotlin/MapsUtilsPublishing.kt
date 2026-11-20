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

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project

/**
 * Publishing settings shared by the Android-only and the Kotlin Multiplatform modules: Maven
 * Central, signing when keys are configured, the public artifactId scheme and the POM.
 */
internal fun MavenPublishBaseExtension.configureMapsUtilsPublishing(project: Project) {
    publishToMavenCentral()
    if (project.findProperty("signing.keyId")?.toString()?.isNotBlank() == true ||
        project.findProperty("signing.secretKeyRingFile")?.toString()?.isNotBlank() == true ||
        project.findProperty("signingInMemoryKey")?.toString()?.isNotBlank() == true
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
