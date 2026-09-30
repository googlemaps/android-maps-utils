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

/**
 * A data class representing a collection of features to be rendered.
 *
 * This acts as a top-level container for all geometric objects and their styles within a single renderable unit.
 *
 * @property features The list of [Feature] objects contained within this scene. Defaults to an empty list.
 */
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * A data class representing a collection of features to be rendered.
 *
 * This acts as a top-level container for all geometric objects and their styles within a single renderable unit.
 *
 * @property features The list of [Feature] objects contained within this scene. Defaults to an empty list.
 */
/**
 * A data class representing a collection of layers to be rendered.
 *
 * This acts as a top-level container for multiple layers.
 *
 * @property layers The list of [Layer] objects contained within this scene. Defaults to an empty list.
 */
public data class DataScene(public val layers: List<DataLayer> = emptyList()) {

    public companion object {
        public fun fromFeatures(features: List<Feature>): DataScene {
            return DataScene(listOf(DataLayer(features)))
        }
    }

    public val features: List<Feature>
        get() = layers.flatMap { it.features }

    public val boundingBox: LatLngBounds? by lazy {
        val boundsBuilder = LatLngBounds.builder()
        var hasPoints = false
        layers.forEach { layer ->
            layer.boundingBox?.let {
                boundsBuilder.include(it.northeast)
                boundsBuilder.include(it.southwest)
                hasPoints = true
            }
        }
        if (hasPoints) boundsBuilder.build() else null
    }
}
