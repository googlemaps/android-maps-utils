package com.google.maps.ktx.core

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
        val polygon = mock<Polygon> {
            on { points } doReturn listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0))
        }
        assertTrue(polygon.contains(LatLng(1.0, 2.2), true))
    }

    @Test
    fun testContainsFalse() {
        val polygon = mock<Polygon> {
            on { points } doReturn listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0))
        }
        assertFalse(polygon.contains(LatLng(1.01, 2.2), true))
    }
}