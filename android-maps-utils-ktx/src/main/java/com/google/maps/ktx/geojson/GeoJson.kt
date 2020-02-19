package com.google.maps.ktx.geojson

import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.geojson.GeoJsonLayer
import org.json.JSONObject

/**
 * Alias for the [GeoJsonLayer] constructor that provides Kotlin named parameters and default
 * values.
 */
inline fun GeoJsonLayer(
    map: GoogleMap,
    geoJsonFile: JSONObject,
    markerManager: MarkerManager? = null,
    polygonManager: PolygonManager? = null,
    polylineManager: PolylineManager? = null,
    groundOverlayManager: GroundOverlayManager? = null
): GeoJsonLayer = GeoJsonLayer(
    map,
    geoJsonFile,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager
)
