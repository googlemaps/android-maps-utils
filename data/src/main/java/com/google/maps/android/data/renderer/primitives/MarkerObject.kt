package com.google.maps.android.data.renderer.primitives

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.renderer.MapObject

class MarkerObject(
    private val googleMap: GoogleMap,
    private val markerOptions: MarkerOptions
) : MapObject {
    private var marker: Marker? = null

    override fun render() {
        marker = googleMap.addMarker(markerOptions)
    }

    override fun remove() {
        marker?.remove()
    }
}
