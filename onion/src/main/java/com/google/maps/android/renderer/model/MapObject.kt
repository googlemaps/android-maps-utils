/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android.renderer.model

/**
 * A common interface for all renderable map objects.
 *
 * This interface serves as a marker for objects that can be added to a
 * [Layer]
 * and rendered by a [com.google.maps.android.renderer.Renderer].
 * Implementations of this interface represent pure data models and should not
 * hold references to Android SDK objects (like
 * [com.google.android.gms.maps.model.Marker]).
 */
interface MapObject {
    /**
     * Gets the type of the map object.
     *
     * @return The type of the map object.
     */
    val type: Type

    /**
     * Gets and sets the visibility of the map object.
     */
    var isVisible: Boolean

    /**
     * Gets and sets the z-index of the map object.
     */
    var zIndex: Float

    /**
     * Enum representing the type of map object.
     */
    enum class Type {
        MARKER,
        POLYLINE,
        POLYGON,
        CIRCLE
    }
}
