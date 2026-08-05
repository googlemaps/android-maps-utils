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
 *
 */

@file:Suppress("NOTHING_TO_INLINE")
package com.google.maps.android.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

/**
 * Converts this LatLng to a [WeightedLatLng]
 */
public inline fun LatLng.toWeightedLatLng(
    intensity: Double = WeightedLatLng.DEFAULT_INTENSITY
): WeightedLatLng =
    WeightedLatLng(this, intensity)

/**
 * Constructs a [HeatmapTileProvider].
 *
 * @throws IllegalStateException when [opacity] is not within the range [0, 1] or if [latLngs] is
 * empty
 */
public inline fun heatmapTileProviderWithData(
    latLngs: Collection<LatLng>,
    radius: Int = HeatmapTileProvider.DEFAULT_RADIUS,
    gradient: Gradient = HeatmapTileProvider.DEFAULT_GRADIENT,
    opacity: Double = HeatmapTileProvider.DEFAULT_OPACITY,
    maxIntensity: Double = 0.0
) : HeatmapTileProvider {
    return HeatmapTileProvider.Builder()
        .data(latLngs)
        .radius(radius)
        .gradient(gradient)
        .opacity(opacity)
        .maxIntensity(maxIntensity)
        .build()
}

/**
 * Constructs a [HeatmapTileProvider].
 *
 * @throws IllegalStateException when [opacity] is not within the range [0, 1] or if [latLngs] is
 * empty
 */
public inline fun heatmapTileProviderWithWeightedData(
    latLngs: Collection<WeightedLatLng>,
    radius: Int = HeatmapTileProvider.DEFAULT_RADIUS,
    gradient: Gradient = HeatmapTileProvider.DEFAULT_GRADIENT,
    opacity: Double = HeatmapTileProvider.DEFAULT_OPACITY,
    maxIntensity: Double = 0.0
) : HeatmapTileProvider {
    return HeatmapTileProvider.Builder()
        .weightedData(latLngs)
        .radius(radius)
        .gradient(gradient)
        .opacity(opacity)
        .maxIntensity(maxIntensity)
        .build()
}
