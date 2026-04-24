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
package com.google.maps.android.data.renderer.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * A data class representing a layer of features.
 *
 * A layer is a collection of features that can be managed independently (added/removed) from the map.
 *
 * @property features The list of [Feature] objects contained within this layer.
 * @property properties Arbitrary properties associated with the layer.
 */
data class DataLayer(
    val features: List<Feature> = emptyList(),
    val properties: Map<String, Any> = emptyMap()
) {
    val boundingBox: LatLngBounds? by lazy {
        val boundsBuilder = LatLngBounds.builder()
        var hasPoints = false
        features.forEach { feature ->
            when (val geometry = feature.geometry) {
                is PointGeometry -> {
                    boundsBuilder.include(LatLng(geometry.point.lat, geometry.point.lng))
                    hasPoints = true
                }
                is LineString -> {
                    geometry.points.forEach {
                        boundsBuilder.include(LatLng(it.lat, it.lng))
                        hasPoints = true
                    }
                }
                is Polygon -> {
                    geometry.outerBoundary.forEach {
                        boundsBuilder.include(LatLng(it.lat, it.lng))
                        hasPoints = true
                    }
                }
                is MultiGeometry -> {
                    // TODO: Implement MultiGeometry bounds calculation if needed
                }
                is GroundOverlay -> {
                    boundsBuilder.include(geometry.latLngBounds.northeast)
                    boundsBuilder.include(geometry.latLngBounds.southwest)
                    hasPoints = true
                }
            }
        }
        if (hasPoints) boundsBuilder.build() else null
    }
}
