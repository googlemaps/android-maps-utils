package com.google.maps.ktx.kml

import android.content.Context
import androidx.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.Renderer
import com.google.maps.android.data.kml.KmlLayer
import java.io.InputStream

/**
 * Alias for the [KmlLayer] constructor that provides Kotlin named parameters and default values.
 */
inline fun kmlLayer(
    map: GoogleMap,
    @RawRes resourceId: Int,
    context: Context,
    markerManager: MarkerManager = MarkerManager(map),
    polygonManager: PolygonManager = PolygonManager(map),
    polylineManager: PolylineManager = PolylineManager(map),
    groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map),
    imagesCache: Renderer.ImagesCache? = null
): KmlLayer = KmlLayer(
    map,
    resourceId,
    context,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager,
    imagesCache
)

/**
 * Alias for the [KmlLayer] constructor that provides Kotlin named parameters and default values.
 */
inline fun kmlLayer(
    map: GoogleMap,
    stream: InputStream,
    context: Context,
    markerManager: MarkerManager = MarkerManager(map),
    polygonManager: PolygonManager = PolygonManager(map),
    polylineManager: PolylineManager = PolylineManager(map),
    groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map),
    imagesCache: Renderer.ImagesCache? = null
): KmlLayer = KmlLayer(
    map,
    stream,
    context,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager,
    imagesCache
)
