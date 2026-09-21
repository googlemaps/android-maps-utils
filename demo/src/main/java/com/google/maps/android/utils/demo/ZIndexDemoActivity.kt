/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.utils.demo

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.utils.demo.databinding.ActivityZindexDemoBinding

// [START maps_android_utils_zindex_shape_interface]
/**
 * Common interface representing a styled map object whose visual layering ([zIndex])
 * can be drawn or refreshed in-place on a [GoogleMap].
 *
 * **Note on zIndex Layering**:
 * - When two shapes of the same type share identical [zIndex] values, the Google Maps SDK resolves
 *   their relative stacking order based on insertion or creation order.
 * - Markers always render above other map overlays (polylines, polygons, circles, and ground overlays),
 *   regardless of the explicit [zIndex] value assigned to those shapes. This is because markers reside
 *   in a separate z-index group. For more details, see the official
 *   [Google Maps Markers Documentation](https://developers.google.com/maps/documentation/android-sdk/marker#z-index).
 */
sealed interface MapShape {
    val id: String
    val name: String
    val zIndex: Float

    /**
     * Renders this shape onto the [map]. If the underlying native Google Maps SDK object
     * has already been created, updates its properties (like [zIndex]) in-place without
     * destroying and recreating the geometry.
     *
     * @param map The active [GoogleMap] instance.
     * @return A copy of this [MapShape] holding the native map handle.
     */
    fun drawOrRefresh(map: GoogleMap): MapShape

    /**
     * Removes the native Google Maps SDK object from the map and releases its reference.
     */
    fun remove()

    /**
     * Creates a copy of this shape with an updated [newZIndex].
     */
    fun withZIndex(newZIndex: Float): MapShape
}

/**
 * Represents a styled Polygon on the map with dynamic [zIndex] control.
 *
 * @property id Unique identifier for the shape in the registry.
 * @property name Human-readable label for UI displays.
 * @property zIndex Current stacking order value (higher draws above lower).
 * @property points Ordered vertices defining the polygon's outer boundary.
 * @property fillColor ARGB integer fill color of the polygon.
 * @property strokeColor ARGB integer stroke border color of the polygon.
 * @property strokeWidth Stroke width in screen pixels.
 * @property nativePolygon The active Google Maps SDK [Polygon] handle, if drawn.
 */
data class PolygonShape(
    override val id: String,
    override val name: String,
    override val zIndex: Float,
    val points: List<LatLng>,
    val fillColor: Int,
    val strokeColor: Int,
    val strokeWidth: Float,
    private val nativePolygon: Polygon? = null,
) : MapShape {

    override fun drawOrRefresh(map: GoogleMap): PolygonShape {
        return if (nativePolygon != null) {
            // Update the existing native object's zIndex directly without recreating the GL shape
            nativePolygon.zIndex = zIndex
            this
        } else {
            val polygon = map.addPolygon(
                PolygonOptions()
                    .addAll(points)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .strokeWidth(strokeWidth)
                    .zIndex(zIndex)
            )
            copy(nativePolygon = polygon)
        }
    }

    override fun remove() {
        nativePolygon?.remove()
    }

    override fun withZIndex(newZIndex: Float): PolygonShape = copy(zIndex = newZIndex)
}

/**
 * Represents a styled Polyline on the map with dynamic [zIndex] control.
 *
 * @property id Unique identifier for the shape in the registry.
 * @property name Human-readable label for UI displays.
 * @property zIndex Current stacking order value (higher draws above lower).
 * @property points Ordered vertices defining the polyline path.
 * @property color ARGB integer color of the line.
 * @property width Stroke width in screen pixels.
 * @property nativePolyline The active Google Maps SDK [Polyline] handle, if drawn.
 */
data class PolylineShape(
    override val id: String,
    override val name: String,
    override val zIndex: Float,
    val points: List<LatLng>,
    val color: Int,
    val width: Float,
    private val nativePolyline: Polyline? = null,
) : MapShape {

    override fun drawOrRefresh(map: GoogleMap): PolylineShape {
        return if (nativePolyline != null) {
            // Update the existing native object's zIndex directly in place
            nativePolyline.zIndex = zIndex
            this
        } else {
            val polyline = map.addPolyline(
                PolylineOptions()
                    .addAll(points)
                    .color(color)
                    .width(width)
                    .zIndex(zIndex)
            )
            copy(nativePolyline = polyline)
        }
    }

    override fun remove() {
        nativePolyline?.remove()
    }

    override fun withZIndex(newZIndex: Float): PolylineShape = copy(zIndex = newZIndex)
}

/**
 * Represents a styled Marker on the map with dynamic [zIndex] control.
 *
 * **Note on zIndex Layering**: Markers always render above other map overlays (polylines, polygons,
 * circles, and ground overlays), regardless of the explicit [zIndex] value assigned to those shapes.
 * This is because markers reside in a separate z-index group; setting [zIndex] on a marker only
 * controls its stacking order relative to other markers. For more details, see the official
 * [Google Maps Markers Documentation](https://developers.google.com/maps/documentation/android-sdk/marker#z-index).
 *
 * @property id Unique identifier for the shape in the registry.
 * @property name Human-readable label for UI displays.
 * @property zIndex Current stacking order value (higher draws above lower relative to other markers).
 * @property position Geographic coordinate of the marker.
 * @property title Info window title text.
 * @property hue Marker color hue (0.0 to 360.0).
 * @property nativeMarker The active Google Maps SDK [Marker] handle, if drawn.
 */
data class MarkerShape(
    override val id: String,
    override val name: String,
    override val zIndex: Float,
    val position: LatLng,
    val title: String,
    val hue: Float,
    private val nativeMarker: Marker? = null,
) : MapShape {

    override fun drawOrRefresh(map: GoogleMap): MarkerShape {
        return if (nativeMarker != null) {
            // Update the existing native marker's zIndex directly in place
            nativeMarker.zIndex = zIndex
            this
        } else {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    .zIndex(zIndex)
            )
            copy(nativeMarker = marker)
        }
    }

    override fun remove() {
        nativeMarker?.remove()
    }

    override fun withZIndex(newZIndex: Float): MarkerShape = copy(zIndex = newZIndex)
}
// [END maps_android_utils_zindex_shape_interface]

// [START maps_android_utils_zindex_sample]
/**
 * Demonstrates how to control the visual layering (z-index) of overlapping map objects
 * (polygons, polylines, and markers) using a unified [MapShape] architecture.
 *
 * Each shape can draw and refresh its [zIndex] in-place without destroying and recreating
 * the underlying Google Maps SDK objects. Shapes are tracked in a [LinkedHashMap] keyed
 * by their unique object ID.
 *
 * **Note on zIndex Layering**: Markers always render above other map overlays (polylines,
 * polygons, circles, and ground overlays), regardless of the explicit [zIndex] value assigned
 * to those shapes. This is because markers reside in a separate z-index group. For more details,
 * see the official [Google Maps Markers Documentation](https://developers.google.com/maps/documentation/android-sdk/marker#z-index).
 *
 * Key API calls:
 * - [com.google.android.gms.maps.model.Polygon.setZIndex]
 * - [com.google.android.gms.maps.model.Polyline.setZIndex]
 * - [com.google.android.gms.maps.model.Marker.setZIndex]
 * - [com.google.android.gms.maps.GoogleMap.addPolygon]
 * - [com.google.android.gms.maps.GoogleMap.addPolyline]
 * - [com.google.android.gms.maps.GoogleMap.addMarker]
 */
class ZIndexDemoActivity : BaseDemoActivity() {

    private lateinit var binding: ActivityZindexDemoBinding

    /**
     * Registry of all active demo shapes, keyed by their unique shape ID.
     * When a shape's z-index is modified, the updated shape replaces the previous entry here.
     */
    private val shapes = LinkedHashMap<String, MapShape>()

    /**
     * Binds a shape's ID to its corresponding UI row widgets.
     */
    private data class ShapeRowBinding(
        val shapeId: String,
        val minusButton: View,
        val plusButton: View,
        val zIndexText: TextView,
    )

    private val rowBindings by lazy {
        listOf(
            ShapeRowBinding(ID_POLYGON_1, binding.polygon1Minus, binding.polygon1Plus, binding.polygon1ZIndexText),
            ShapeRowBinding(ID_POLYGON_2, binding.polygon2Minus, binding.polygon2Plus, binding.polygon2ZIndexText),
            ShapeRowBinding(ID_POLYLINE_1, binding.polyline1Minus, binding.polyline1Plus, binding.polyline1ZIndexText),
            ShapeRowBinding(ID_POLYLINE_2, binding.polyline2Minus, binding.polyline2Plus, binding.polyline2ZIndexText),
            ShapeRowBinding(ID_MARKER, binding.markerMinus, binding.markerPlus, binding.markerZIndexText),
        )
    }

    companion object {
        const val ID_POLYGON_1 = "polygon_1"
        const val ID_POLYGON_2 = "polygon_2"
        const val ID_POLYLINE_1 = "polyline_1"
        const val ID_POLYLINE_2 = "polyline_2"
        const val ID_MARKER = "marker"

        private val INITIAL_Z_INDICES = mapOf(
            ID_POLYGON_1 to 1.0f,
            ID_POLYGON_2 to 2.0f,
            ID_POLYLINE_1 to 3.0f,
            ID_POLYLINE_2 to 4.0f,
            ID_MARKER to 5.0f,
        )
    }

    override fun getLayoutId(): Int = R.layout.activity_zindex_demo

    override fun startDemo(isRestore: Boolean) {
        val rootView = (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
        binding = ActivityZindexDemoBinding.bind(rootView)

        val map = map ?: return

        // Center on the demo shapes in San Francisco
        val center = LatLng(37.7749, -122.4194)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))

        setupFeatures(map, center)
        setupControls()
    }

    /**
     * Initializes the [MapShape] instances, draws them on the [map], and populates [shapes].
     */
    private fun setupFeatures(map: GoogleMap, center: LatLng) {
        val lat = center.latitude
        val lng = center.longitude

        val initialShapes = listOf(
            // Polygon 1 (Coral) - covers the western half
            PolygonShape(
                id = ID_POLYGON_1,
                name = getString(R.string.zindex_polygon_1),
                zIndex = 1.0f,
                points = listOf(
                    LatLng(lat - 0.005, lng - 0.008),
                    LatLng(lat + 0.005, lng - 0.008),
                    LatLng(lat + 0.005, lng + 0.002),
                    LatLng(lat - 0.005, lng + 0.002),
                    LatLng(lat - 0.005, lng - 0.008),
                ),
                fillColor = 0xCCFF5722.toInt(),
                strokeColor = 0xFFD84315.toInt(),
                strokeWidth = 6f,
            ),
            // Polygon 2 (Teal) - overlaps Polygon 1 in the center and extends east
            PolygonShape(
                id = ID_POLYGON_2,
                name = getString(R.string.zindex_polygon_2),
                zIndex = 2.0f,
                points = listOf(
                    LatLng(lat - 0.004, lng - 0.002),
                    LatLng(lat + 0.004, lng - 0.002),
                    LatLng(lat + 0.004, lng + 0.008),
                    LatLng(lat - 0.004, lng + 0.008),
                    LatLng(lat - 0.004, lng - 0.002),
                ),
                fillColor = 0xCC00BCD4.toInt(),
                strokeColor = 0xFF0097A7.toInt(),
                strokeWidth = 6f,
            ),
            // Polyline 1 (Gold) - diagonal line crossing both polygons
            PolylineShape(
                id = ID_POLYLINE_1,
                name = getString(R.string.zindex_polyline_1),
                zIndex = 3.0f,
                points = listOf(
                    LatLng(lat - 0.006, lng - 0.009),
                    LatLng(lat, lng),
                    LatLng(lat + 0.006, lng + 0.009),
                ),
                color = 0xFFFFD600.toInt(),
                width = 16f,
            ),
            // Polyline 2 (Purple) - horizontal line cutting across the overlap
            PolylineShape(
                id = ID_POLYLINE_2,
                name = getString(R.string.zindex_polyline_2),
                zIndex = 4.0f,
                points = listOf(
                    LatLng(lat + 0.002, lng - 0.009),
                    LatLng(lat - 0.002, lng + 0.009),
                ),
                color = 0xFF9C27B0.toInt(),
                width = 16f,
            ),
            // Center Marker (Green) - positioned at the exact intersection of both lines and polygons
            MarkerShape(
                id = ID_MARKER,
                name = getString(R.string.zindex_marker),
                zIndex = 5.0f,
                position = LatLng(lat, lng),
                title = "Intersection Point",
                hue = BitmapDescriptorFactory.HUE_GREEN,
            ),
        )

        // Draw each shape on the map and store the rendered object in the shapes registry
        for (shape in initialShapes) {
            val rendered = shape.drawOrRefresh(map)
            shapes[rendered.id] = rendered
        }
    }

    /**
     * Binds stepper buttons and initial z-index labels for each shape.
     */
    private fun setupControls() {
        for (row in rowBindings) {
            val shape = shapes[row.shapeId] ?: continue
            row.zIndexText.text = getString(R.string.zindex_value_fmt, shape.zIndex)
            row.minusButton.setOnClickListener { updateShapeZIndex(row.shapeId, -1.0f) }
            row.plusButton.setOnClickListener { updateShapeZIndex(row.shapeId, +1.0f) }
        }
        binding.resetButton.setOnClickListener { resetZIndices() }
    }

    /**
     * Resets all shapes to their initial default z-index values and updates the map in-place.
     */
    private fun resetZIndices() {
        val map = map ?: return
        for ((shapeId, defaultZIndex) in INITIAL_Z_INDICES) {
            val currentShape = shapes[shapeId] ?: continue
            val updatedShape = currentShape.withZIndex(defaultZIndex)
            val renderedShape = updatedShape.drawOrRefresh(map)
            shapes[shapeId] = renderedShape
            rowBindings.firstOrNull { it.shapeId == shapeId }?.zIndexText?.text =
                getString(R.string.zindex_value_fmt, renderedShape.zIndex)
        }
    }

    // [START maps_android_utils_zindex_update_sample]
    /**
     * Updates the z-index of the shape identified by [shapeId] by [delta].
     * Refreshes the shape on the map in-place and replaces the entry in [shapes].
     */
    private fun updateShapeZIndex(shapeId: String, delta: Float) {
        val currentShape = shapes[shapeId] ?: return
        val map = map ?: return

        // 1. Create an updated shape with the new zIndex
        val updatedShape = currentShape.withZIndex(currentShape.zIndex + delta)

        // 2. Refresh the shape on the map (updates native map object in-place)
        val renderedShape = updatedShape.drawOrRefresh(map)

        // 3. Replace the object in the map data structure with the same object id
        shapes[shapeId] = renderedShape

        // 4. Update the corresponding UI label
        rowBindings.firstOrNull { it.shapeId == shapeId }?.zIndexText?.text =
            getString(R.string.zindex_value_fmt, renderedShape.zIndex)
    }
    // [END maps_android_utils_zindex_update_sample]
}
// [END maps_android_utils_zindex_sample]
