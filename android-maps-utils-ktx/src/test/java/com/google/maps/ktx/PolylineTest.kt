package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.*
import org.junit.Test

class PolylineTest {
    private val earthRadius = 6371009.0

    @Test
    fun `test that contains returns true`() {
        val line = mockPolyline(listOf(LatLng(1.0, 0.0), LatLng(3.0, 0.0)))
        assertTrue(line.contains(LatLng(2.0, 0.0)))
    }

    @Test
    fun `test that contains returns true with tolerance`() {
        val line = mockPolyline(listOf(LatLng(1.0, 0.0), LatLng(3.0, 0.0)))
        assertTrue(line.contains(LatLng(1.0, 0.00000001)))
    }

    @Test
    fun `test that contains returns false`() {
        val line = mockPolyline(listOf(LatLng(1.0, 0.0), LatLng(3.0, 0.0)))
        assertFalse(line.contains(LatLng(4.0, 0.0)))
    }

    @Test
    fun `validate spherical path length`() {
        assertEquals(0, mockPolyline(emptyList()).sphericalPathLength)
        val polyline = mockPolyline(listOf(LatLng(0.0, 0.0), LatLng(0.1, 0.1)))
        val expectation = earthRadius * Math.sqrt(2.0) * Math.toRadians(0.1)
        assertEquals(expectation, polyline.sphericalPathLength, 1e-6)
    }

    private fun mockPolyline(p: List<LatLng>, geodesic: Boolean = true) = mock<Polyline> {
        on { points } doReturn p
        on { isGeodesic } doReturn geodesic
    }
}