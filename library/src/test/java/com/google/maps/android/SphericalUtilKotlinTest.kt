package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs

/**
 * Tests for [SphericalUtil] that are written in Kotlin.
 */
class SphericalUtilKotlinTest {

    val testPolygon = """
        -104.9596325,39.7543772,0
        -104.9596969,39.7448581,0
        -104.959375,39.7446271,0
        -104.959096,39.7443136,0
        -104.9588171,39.7440166,0
        -104.9581305,39.7439176,0
        -104.9409429,39.7438681,0
        -104.9408785,39.7543277,0
        -104.9596325,39.7543772,0
    """.trimIndent().lines().map {
        val (lng, lat, _) = it.split(",")
        LatLng(lat.toDouble(), lng.toDouble())
    }

    // The expected length (perimeter) of the test polygon in meters.
    private val EXPECTED_LENGTH_METERS = 5474.0

    // The expected area of the test polygon in square meters.
    private val EXPECTED_AREA_SQ_METERS = 1859748.0

    // A tolerance for comparing floating-point numbers.
    private val TOLERANCE = 1.0 // 1 meter or 1 sq meter
    
    /**
     * Tests the `computeLength` method with the polygon from the KML file.
     */
    @Test
    fun testComputeLengthWithKmlPolygon() {
        val calculatedLength = SphericalUtil.computeLength(testPolygon)
        assertThat(calculatedLength).isWithin(TOLERANCE).of(EXPECTED_LENGTH_METERS)
    }

    /**
     * Tests the `computeSignedArea` method with the polygon from the KML file.
     * Note: We test the absolute value since computeArea simply wraps computeSignedArea with abs().
     */
    @Test
    fun testComputeSignedAreaWithKmlPolygon() {
        val calculatedSignedArea = SphericalUtil.computeSignedArea(testPolygon)
        assertThat(abs(calculatedSignedArea)).isWithin(TOLERANCE).of(EXPECTED_AREA_SQ_METERS)
    }
}