package com.google.maps.ktx.core

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class PolygonOptionsTest {
    @Test
    fun testBuilder() {
        val polygonOptions = buildPolygonOptions {
            strokeWidth(1.0f)
            strokeColor(Color.BLACK)
            add(LatLng(0.0, 0.0))
        }
        assertEquals(1.0f, polygonOptions.strokeWidth)
        assertEquals(Color.BLACK, polygonOptions.strokeColor)
        assertEquals(listOf(LatLng(0.0, 0.0)), polygonOptions.points)
    }
}