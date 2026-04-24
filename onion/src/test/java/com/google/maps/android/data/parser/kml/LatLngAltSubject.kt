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
import kotlin.math.abs

fun assertThat(latLngAlt: LatLngAlt?): LatLngAltSubject {
    return Truth.assertAbout(latLngAltSubjectFactory()).that(latLngAlt)
}

fun latLngAltSubjectFactory(): Subject.Factory<LatLngAltSubject, LatLngAlt> {
    return Subject.Factory { metaData, target ->
        LatLngAltSubject(
            metaData,
            target
        )
    }
}

class LatLngAltSubject(
    metadata: FailureMetadata,
    private val actual: LatLngAlt?
) : Subject(metadata, actual) {

    fun isNear(expected: LatLngAlt) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null LatLngAlt, but was null"))
            return
        }
        val tolerance = 1e-6
        if (abs(actual.latitude - expected.latitude) > tolerance ||
            abs(actual.longitude - expected.longitude) > tolerance
        ) {
            failWithActual(
                Fact.fact("expected to be near", expected.toString()),
                Fact.fact("but was", actual.toString())
            )
        }

        if (actual.altitude != null && expected.altitude != null) {
            if (abs(actual.altitude - expected.altitude) > tolerance) {
                failWithActual(
                    Fact.fact("expected altitude to be near", expected.altitude.toString()),
                    Fact.fact("but was", actual.altitude.toString())
                )
            }
        } else if (actual.altitude != null || expected.altitude != null) {
            failWithActual(
                Fact.simpleFact("expected altitudes to both be null or both be non-null"),
                Fact.fact("actual altitude", actual.altitude?.toString() ?: "null"),
                Fact.fact("expected altitude", expected.altitude?.toString() ?: "null")
            )
        }
    }
}