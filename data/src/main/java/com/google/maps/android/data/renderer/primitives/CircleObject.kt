package com.google.maps.android.data.renderer.primitives

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.maps.android.data.renderer.MapObject

class CircleObject(
    private val googleMap: GoogleMap,
    private val circleOptions: CircleOptions
) : MapObject {
    private var circle: Circle? = null

    override fun render() {
        circle = googleMap.addCircle(circleOptions)
    }

    override fun remove() {
        circle?.remove()
    }
}
