package com.google.maps.android.data.renderer

import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.data.renderer.Layer

class Renderer(private val googleMap: GoogleMap) {
    private val layers = mutableListOf<Layer>()

    fun addLayer(layer: Layer) {
        layers.add(layer)
        layer.addLayerToMap()
    }

    fun removeLayer(layer: Layer) {
        layers.remove(layer)
        layer.removeLayerFromMap()
    }

    fun getLayers(): List<Layer> {
        return layers.toList()
    }
}
