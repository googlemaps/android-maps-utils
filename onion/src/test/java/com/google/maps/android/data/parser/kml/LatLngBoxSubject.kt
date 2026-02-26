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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

fun assertThat(latLonBox: LatLonBox?): LatLonBoxSubject {
    return Truth.assertAbout(latLonBoxSubjectFactory()).that(latLonBox)
}

fun latLonBoxSubjectFactory(): Subject.Factory<LatLonBoxSubject, LatLonBox> {
    return Subject.Factory { metaData, target ->
        LatLonBoxSubject(
            metaData,
            target
        )
    }
}

class LatLonBoxSubject(
    metadata: FailureMetadata,
    private val actual: LatLonBox?
) : Subject(metadata, actual) {

    fun isNear(expected: LatLonBox) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null LatLonBox, but was null"))
            return
        }

        val tolerance = 1e-6

        // specific logic for nullable rotation
        val rotationMismatch = if (actual.rotation != null && expected.rotation != null) {
            // If both exist, check tolerance
            abs(actual.rotation - expected.rotation) > tolerance
        } else {
            // If one is null and the other isn't, this returns true (fail).
            // If both are null, this returns false (pass).
            actual.rotation != expected.rotation
        }

        // Check if any of the 4 cardinal boundaries exceed the tolerance
        if (abs(actual.north - expected.north) > tolerance ||
            abs(actual.south - expected.south) > tolerance ||
            abs(actual.east - expected.east) > tolerance ||
            abs(actual.west - expected.west) > tolerance ||
            rotationMismatch
        ) {
            failWithActual(
                Fact.fact("expected to be near", expected.toString()),
                Fact.fact("but was", actual.toString())
            )
        }
    }

    /**
     * Asserts that the given [LatLngAlt] is contained within the actual bounds, allowing for
     * a small tolerance to account for floating-point inaccuracies.
     *
     * If rotation is present, the point is transformed into the box's local coordinate space
     * for verification.
     */
    fun containsWithTolerance(expected: LatLngAlt) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null LatLonBox, but was null"))
            return
        }

        val tolerance = 1e-5
        val rotation = actual.rotation

        val contains = if (rotation != null && abs(rotation) > tolerance) {
            // --- Rotated Logic ---
            // 1. Calculate the center of the box
            val centerLat = (actual.north + actual.south) / 2.0
            val centerLon = (actual.east + actual.west) / 2.0 // Note: Assumes simple average (watch for Date Line if box crosses 180)

            // 2. Calculate distance from center to point (dLat, dLon)
            val dLat = expected.latitude - centerLat
            val dLon = expected.longitude - centerLon

            // 3. Convert rotation to radians and reverse direction (un-rotate the point)
            // Standard rotation matrix is CCW. If your map rotation is CW, remove the negative sign.
            val angleRad = -rotation * (PI / 180.0)
            val cosA = cos(angleRad)
            val sinA = sin(angleRad)

            // 4. Rotate the point's delta vector
            // x' = x cos - y sin, y' = x sin + y cos (where x=lon, y=lat)
            val rotatedLon = dLon * cosA - dLat * sinA
            val rotatedLat = dLon * sinA + dLat * cosA

            // 5. Check against original half-extents
            val halfSpanLat = (actual.north - actual.south) / 2.0
            val halfSpanLon = (actual.east - actual.west) / 2.0

            // Check if the un-rotated point is within the axis-aligned bounds (+ tolerance)
            abs(rotatedLat) <= halfSpanLat + tolerance &&
                    abs(rotatedLon) <= halfSpanLon + tolerance

        } else {
            // --- Standard Logic (No Rotation) ---
            actual.south - tolerance <= expected.latitude &&
                    expected.latitude <= actual.north + tolerance &&
                    isLongitudeWithinBounds(expected.longitude, actual, tolerance)
        }

        if (!contains) {
            failWithActual("expected to contain", expected)
        }
    }

    /**
     * Checks if a longitude is within the bounds, correctly handling cases where the bounds
     * cross the antimeridian (e.g., from 160 to -160).
     */
    private fun isLongitudeWithinBounds(longitude: Double, bounds: LatLonBox, tolerance: Double): Boolean {
        return if (bounds.west <= bounds.east) {
            // Standard case (e.g., from 10 to 20).
            longitude >= bounds.west - tolerance && longitude <= bounds.east + tolerance
        } else {
            // Case where the bounds cross the antimeridian (e.g., from 160 to -160).
            longitude >= bounds.west - tolerance || longitude <= bounds.east + tolerance
        }
    }
}