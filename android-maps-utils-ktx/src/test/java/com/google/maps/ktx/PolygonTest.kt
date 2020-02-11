package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolygonTest {
    @Test
    fun testContainsTrue() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertTrue(polygon.contains(LatLng(1.0, 2.2)))
    }

    @Test
    fun testContainsFalse() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertFalse(polygon.contains(LatLng(1.01, 2.2)))
    }

    @Test
    fun testIsOnEdgeTrue() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertTrue(polygon.isOnEdge(LatLng(1.0, 2.2)))

        // Tolerance
        assertTrue(polygon.isOnEdge(LatLng(1.0000005, 2.2)))
    }

    @Test
    fun testIsOnEdgeFalse() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertFalse(polygon.isOnEdge(LatLng(3.0, 2.2)))
    }

    private fun mockPolygon(p: List<LatLng>, geodesic: Boolean = true) = mock<Polygon> {
        on { points } doReturn p
        on { isGeodesic } doReturn geodesic
    }
}