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
 * A sealed interface representing a style for a geometric object.
 *
 * This is a platform-agnostic representation of styling that can be applied to a rendered shape.
 */
sealed interface Style

/**
 * A data class representing the style for a PointGeometry.
 *
 * @property color The color of the point, as an integer (e.g., ARGB) or hex string. Defaults to black.
 * @property iconUrl The URL or resource identifier for a custom marker icon. Optional.
 * @property heading The heading of the icon in degrees clockwise from north. Optional.
 * @property anchorU The U-coordinate of the icon's anchor point, as a ratio of the icon's width (0.0 to 1.0). Defaults to 0.5 (center).
 * @property anchorV The V-coordinate of the icon's anchor point, as a ratio of the icon's height (0.0 to 1.0). Defaults to 1.0 (bottom).
 * @property scale The scale factor for the icon. Defaults to 1.0.
 */
data class PointStyle(
    val color: Int = 0xFF000000.toInt(), // Default to black
    val iconUrl: String? = null,
    val heading: Float? = null,
    val anchorU: Float = 0.5f,
    val anchorV: Float = 1.0f,
    val scale: Float = 1.0f
) : Style

/**
 * A data class representing the style for a LineString.
 *
 * @property color The color of the line, as an integer (e.g., ARGB) or hex string. Defaults to black.
 * @property width The width of the line in pixels. Defaults to 1.0.
 * @property geodesic Indicates whether the line should be drawn as a geodesic (true) or a straight line (false). Defaults to false.
 */
data class LineStyle(
    val color: Int = 0xFF000000.toInt(), // Default to black
    val width: Float = 1.0f,
    val geodesic: Boolean = false
) : Style

/**
 * A data class representing the style for a Polygon.
 *
 * @property fillColor The fill color of the polygon, as an integer (e.g., ARGB) or hex string. Defaults to transparent black.
 * @property strokeColor The stroke color of the polygon, as an integer (e.g., ARGB) or hex string. Defaults to black.
 * @property strokeWidth The stroke width of the polygon in pixels. Defaults to 1.0.
 * @property geodesic Indicates whether the polygon's stroke should be drawn as a geodesic (true) or a straight line (false). Defaults to false.
 */
data class PolygonStyle(
    val fillColor: Int = 0x00000000,
    val strokeColor: Int = 0xFF000000.toInt(), // Default to black
    val strokeWidth: Float = 1.0f,
    val geodesic: Boolean = false
) : Style
