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

package com.google.maps.android.data.parser.kml

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth

fun assertThat(point: Point?): PointSubject {
    return Truth.assertAbout(pointSubjectFactory()).that(point)
}

fun pointSubjectFactory(): Subject.Factory<PointSubject, Point> {
    return Subject.Factory { metaData, target ->
        PointSubject(
            metaData,
            target
        )
    }
}

class PointSubject(
    metadata: FailureMetadata,
    private val actual: Point?
) : Subject(metadata, actual) {

    fun isNear(expected: LatLngAlt) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null Point, but was null"))
            return
        }

        assertThat(actual.coordinates).isNear(expected)
    }
}