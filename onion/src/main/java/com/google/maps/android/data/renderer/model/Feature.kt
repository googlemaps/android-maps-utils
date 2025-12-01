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
package com.google.maps.android.data.renderer.model

/**
 * A data class representing a single feature, which combines a geometry, its style, and arbitrary properties.
 *
 * This is a platform-agnostic representation of a feature to be rendered on a map.
 *
 * @property geometry The geometric object of the feature.
 * @property style The style to be applied to the feature. Can be null if no specific style is defined for the feature.
 * @property properties A map of arbitrary properties associated with the feature. Defaults to an empty map.
 */
data class Feature(
    val geometry: Geometry,
    val style: Style? = null,
    val properties: Map<String, Any> = emptyMap()
)
