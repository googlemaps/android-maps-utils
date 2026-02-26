/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.renderer

import com.google.maps.android.renderer.model.Layer

/**
 * Interface for a renderer that can draw [Layer]s onto a map.
 *
 * Implementations of this interface handle the specifics of how to render
 * the abstract [com.google.maps.android.renderer.model.MapObject]s
 * contained in a layer to a specific map target (e.g., GoogleMap, GoogleMap
 * Compose, etc.).
 */
interface Renderer {
    /**
     * Adds a layer to the renderer. The layer's contents should be drawn on the
     * map.
     *
     * @param layer The layer to add.
     */
    fun addLayer(layer: Layer)

    /**
     * Removes a layer from the renderer. The layer's contents should be removed
     * from the map.
     *
     * @param layer The layer to remove.
     * @return True if the layer was removed, false otherwise.
     */
    fun removeLayer(layer: Layer): Boolean

    /**
     * Gets all layers currently managed by the renderer.
     *
     * @return A collection of layers.
     */
    fun getLayers(): Collection<Layer>

    /**
     * Clears all layers from the renderer and removes their contents from the map.
     */
    fun clear()
}
