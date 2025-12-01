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