package com.google.maps.android.data.renderer.primitives

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.renderer.MapObject

class PolylineObject(
    private val googleMap: GoogleMap,
    private val polylineOptions: PolylineOptions
) : MapObject {
    private var polyline: Polyline? = null

    override fun render() {
        polyline = googleMap.addPolyline(polylineOptions)
    }

    override fun remove() {
        polyline?.remove()
    }
}
