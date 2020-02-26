package com.google.maps.ktx.geometry

import com.google.maps.android.geometry.Point
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PointTest {

    private lateinit var point: Point

    @Before
    fun setUp() {
        point = Point(1.0, 2.0)
    }

    @Test
    fun `destructure x`() {
        val (x, _) = point
        assertEquals(1.0, x)
    }

    @Test
    fun `destructure y`() {
        val (_, y) = point
        assertEquals(2.0, y)
    }
}