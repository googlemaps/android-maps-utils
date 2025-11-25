/*
 * Copyright 2025 Google LLC
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
import com.google.maps.android.data.renderer.model.Scene

/**
 * An interface defining the contract for rendering a [Scene] of geographic features.
 *
 * Implementations of this interface are responsible for translating the platform-agnostic
 * [Scene] and [Feature] models into specific map objects (e.g., Markers, Polylines, Polygons)
 * for a given mapping platform.
 */
interface Renderer {

    /**
     * Renders an entire [Scene] to the map.
     *
     * This method should clear any previously rendered features and draw all features
     * present in the provided [Scene].
     *
     * @param scene The [Scene] to be rendered.
     */
    fun render(scene: Scene)

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
     * Clears all currently rendered features from the map.
     */
    fun clear()
}
