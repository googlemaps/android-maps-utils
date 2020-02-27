package com.google.maps.ktx.geometry

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PointTest {

    private lateinit var point: Point
    private lateinit var projection: SphericalMercatorProjection

    @Before
    fun setUp() {
        point = Point(1.0, 2.0)
        projection = mock {
            on { toLatLng(any()) } doReturn LatLng(0.0, 0.0)
        }
    }

    @Test
    fun `destructure x`() {
        val (x, _) = point
        assertEquals(1.0, x, 1e-6)
    }

    @Test
    fun `destructure y`() {
        val (_, y) = point
        assertEquals(2.0, y, 1e-6)
    }

    @Test
    fun `validate toLatLng passthrough`() {
        point.toLatLng(projection)
        verify(projection).toLatLng(point)
    }
}