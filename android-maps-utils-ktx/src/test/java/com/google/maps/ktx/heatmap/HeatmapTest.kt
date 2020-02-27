package com.google.maps.ktx.heatmap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.WeightedLatLng
import com.google.maps.ktx.heatmaps.toWeightedLatLng
import org.junit.Assert.*
import org.junit.Test

class HeatmapTest {
    @Test
    fun `to WeightedLatLng converts correctly`() {
        val latLng = LatLng(1.0, 2.0)

        weightedLatLngEquals(WeightedLatLng(latLng), latLng.toWeightedLatLng())
        weightedLatLngEquals(
            WeightedLatLng(latLng, 2.0),
            latLng.toWeightedLatLng(intensity = 2.0)
        )
    }

    private fun weightedLatLngEquals(lhs: WeightedLatLng, rhs: WeightedLatLng) {
        assertEquals(lhs.point.x, rhs.point.x, 1e-6)
        assertEquals(lhs.point.y, rhs.point.y, 1e-6)
        assertEquals(lhs.intensity, rhs.intensity, 1e-6)
    }
}