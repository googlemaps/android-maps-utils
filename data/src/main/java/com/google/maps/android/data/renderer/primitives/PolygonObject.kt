package com.google.maps.android.data.renderer.primitives

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.data.renderer.MapObject

class PolygonObject(
    private val googleMap: GoogleMap,
    private val polygonOptions: PolygonOptions
) : MapObject {
    private var polygon: Polygon? = null

    override fun render() {
        polygon = googleMap.addPolygon(polygonOptions)
    }

    override fun remove() {
        polygon?.remove()
    }
}
