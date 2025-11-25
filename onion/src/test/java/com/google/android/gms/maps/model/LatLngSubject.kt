package com.google.android.gms.maps.model

// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.maps.android.SphericalUtil
import kotlin.math.abs

fun assertThat(latLng: LatLng): LatLngSubject {
    return Truth.assertAbout(latLngSubjectFactory()).that(latLng)
}

fun latLngSubjectFactory(): Subject.Factory<LatLngSubject, LatLng> {
    return Subject.Factory { metaData, target ->
        LatLngSubject(
            metaData,
            target
        )
    }
}

/**
 * A custom Truth subject for asserting properties of [LatLng] objects.
 *
 * To use, static import [assertThat] and call assertions on the subject.
 * ```
 * val actual = LatLng(10.0, 20.0)
 * val expected = LatLng(10.000001, 20.000001)
 * assertThat(actual).isNear(expected)
 * ```
 */
class LatLngSubject(
    failureMetadata: FailureMetadata,
    private val actual: LatLng?
) : Subject(failureMetadata, actual) {

    /**
     * Asserts that the actual [LatLng] is within a small tolerance of the expected value.
     * This is useful for accounting for floating-point inaccuracies.
     */
    fun isNear(expected: LatLng) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null LatLng, but was null"))
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
    }

    /**
     * Starts a chained assertion to check the distance of the actual [LatLng] from an expected value.
     */
    fun isWithin(tolerance: Number): LatLngDistanceSubject {
        return LatLngDistanceSubject(tolerance.toDouble())
    }

    /**
     * A subject for asserting the distance between two [LatLng] objects.
     */
    inner class LatLngDistanceSubject(private val tolerance: Double) {
        /**
         * Asserts that the actual [LatLng] is within [tolerance] meters of the [expected] [LatLng].
         */
        fun of(expected: LatLng) {
            if (actual == null) {
                failWithActual(Fact.simpleFact("expected a non-null LatLng, but was null"))
                return
            }
            val distance = SphericalUtil.computeDistanceBetween(actual, expected)
            if (distance > tolerance) {
                failWithActual(
                    Fact.fact("expected to be within $tolerance meters of", expected.toString()),
                    Fact.fact("but was $distance meters away from", actual.toString())
                )
            }
        }
    }
}
