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
        assertEquals(WeightedLatLng(latLng), latLng.toWeightedLatLng())
        assertEquals(WeightedLatLng(latLng, 2.0), latLng.toWeightedLatLng(intensity = 2.0))
    }
}