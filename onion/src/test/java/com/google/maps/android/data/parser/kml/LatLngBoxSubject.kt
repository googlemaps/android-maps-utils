package com.google.maps.android.data.parser.kml

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import kotlin.math.abs

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

        // Check if any of the 4 cardinal boundaries exceed the tolerance
        if (abs(actual.north - expected.north) > tolerance ||
            abs(actual.south - expected.south) > tolerance ||
            abs(actual.east - expected.east) > tolerance ||
            abs(actual.west - expected.west) > tolerance
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
     */
    fun containsWithTolerance(expected: LatLngAlt) {
        if (actual == null) {
            failWithActual(Fact.simpleFact("expected a non-null LatLonBox, but was null"))
            return
        }

        val tolerance = 1e-5
        val contains = actual.south - tolerance <= expected.latitude &&
                expected.latitude <= actual.north + tolerance &&
                isLongitudeWithinBounds(expected.longitude, actual, tolerance)
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