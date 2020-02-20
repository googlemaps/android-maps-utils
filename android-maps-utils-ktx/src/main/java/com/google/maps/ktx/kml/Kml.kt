package com.google.maps.ktx.kml

import androidx.annotation.RawRes
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.kml.KmlLayer
import java.io.InputStream

/**
 * Alias for the [KmlLayer] constructor that provides Kotlin named parameters and default values.
 */
inline fun kmlLayer(
    map: GoogleMap,
    @RawRes resourceId: Int,
    activity: FragmentActivity,
    markerManager: MarkerManager = MarkerManager(map),
    polygonManager: PolygonManager = PolygonManager(map),
    polylineManager: PolylineManager = PolylineManager(map),
    groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map)
) : KmlLayer = KmlLayer(
    map,
    resourceId,
    activity,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager
)

/**
 * Alias for the [KmlLayer] constructor that provides Kotlin named parameters and default values.
 */
inline fun kmlLayer(
    map: GoogleMap,
    stream: InputStream,
    activity: FragmentActivity,
    markerManager: MarkerManager = MarkerManager(map),
    polygonManager: PolygonManager = PolygonManager(map),
    polylineManager: PolylineManager = PolylineManager(map),
    groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map)
) : KmlLayer = KmlLayer(
    map,
    stream,
    activity,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager
)
