package com.google.maps.ktx.core

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineTest {
    @Test
    fun testContainsTrue() {
        val line = mockPolyline(listOf(LatLng(1.0, 0.0), LatLng(3.0, 0.0)))
        assertTrue(line.contains(LatLng(2.0, 0.0)))
    }

    @Test
    fun testContainsTrueWithTolerance() {
        val line = mockPolyline(listOf(LatLng(1.0, 0.0), LatLng(3.0, 0.0)))
        assertTrue(line.contains(LatLng(1.0, 0.00000001)))
    }

    @Test
    fun testContainsFalse() {
        val line = mockPolyline(listOf(LatLng(1.0, 0.0), LatLng(3.0, 0.0)))
        assertFalse(line.contains(LatLng(4.0, 0.0)))
    }

    private fun mockPolyline(p: List<LatLng>, geodesic: Boolean = true) = mock<Polyline> {
        on { points } doReturn p
        on { isGeodesic } doReturn geodesic
    }
}