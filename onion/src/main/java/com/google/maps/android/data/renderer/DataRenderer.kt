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
package com.google.maps.android.data.renderer

import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.DataScene
import com.google.maps.android.data.renderer.model.DataLayer

/**
 * An interface defining the contract for rendering a [Scene] of geographic features.
 *
 * Implementations of this interface are responsible for translating the platform-agnostic
 * [Scene] and [Feature] models into specific map objects (e.g., Markers, Polylines, Polygons)
 * for a given mapping platform.
 */
interface DataRenderer {

    /**
     * Renders an entire [DataScene] to the map.
     *
     * @param scene The scene to render.
     */
    fun render(scene: DataScene)

    /**
     * Adds a single [Feature] to the map.
     *
     * @param feature The [Feature] to add.
     */
    fun addFeature(feature: Feature)

    /**
     * Removes a single [Feature] from the map.
     *
     * @param feature The [Feature] to remove.
     */
    fun removeFeature(feature: Feature)

    /**
     * Adds a [DataLayer] to the map.
     *
     * @param layer The [DataLayer] to add.
     */
    fun addLayer(layer: DataLayer)

    /**
     * Removes a [DataLayer] from the map.
     *
     * @param layer The [DataLayer] to remove.
     */
    fun removeLayer(layer: DataLayer)

    /**
     * Clears all currently rendered features from the map.
     */
    fun clear()
}
