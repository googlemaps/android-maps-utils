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

package com.google.maps.android.data.parser

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.renderer.model.Point
import kotlin.math.abs

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

    fun hasAltitude() {
        if (actual?.alt == null) {
            failWithActual(Fact.simpleFact("expected to have altitude, but was null"))
        }
    }

    fun hasAltitudeWithin(tolerance: Double, expected: Double) {
        hasAltitude()
        if (actual?.alt?.let { abs(it - expected) > tolerance } == true) {
            failWithActual(
                Fact.fact("expected altitude to be within $tolerance of", expected.toString()),
                Fact.fact("but was", actual.alt.toString())
            )
        }
    }

    fun isNear(expected: Point) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null Point, but was null"))
            return
        }
        val tolerance = 1e-6
        if (abs(actual.lat - expected.lat) > tolerance ||
            abs(actual.lng - expected.lng) > tolerance
        ) {
            failWithActual(
                Fact.fact("expected to be near", expected.toString()),
                Fact.fact("but was", actual.toString())
            )
        }

        if (actual.alt != null && expected.alt != null) {
            if (abs(actual.alt - expected.alt) > tolerance) {
                failWithActual(
                    Fact.fact("expected altitude to be near", expected.alt.toString()),
                    Fact.fact("but was", actual.alt.toString())
                )
            }
        } else if (actual.alt != null || expected.alt != null) {
            failWithActual(
                Fact.simpleFact("expected altitudes to both be null or both be non-null"),
                Fact.fact("actual altitude", actual.alt?.toString() ?: "null"),
                Fact.fact("expected altitude", expected.alt?.toString() ?: "null")
            )
        }
    }

    fun isWithin(tolerance: Number): PointDistanceSubject {
        return PointDistanceSubject(tolerance.toDouble())
    }

    inner class PointDistanceSubject(private val tolerance: Double) {
        fun of(expected: Point) {
            if (actual == null) {
                failWithActual(Fact.simpleFact("expected a non-null Point, but was null"))
                return
            }
            val distance = SphericalUtil.computeDistanceBetween(
                LatLng(actual.lat, actual.lng),
                LatLng(expected.lat, expected.lng)
            )
            if (distance > tolerance) {
                failWithActual(
                    Fact.fact("expected to be within $tolerance meters of", expected.toString()),
                    Fact.fact("but was $distance meters away from", actual.toString())
                )
            }
        }
    }
}