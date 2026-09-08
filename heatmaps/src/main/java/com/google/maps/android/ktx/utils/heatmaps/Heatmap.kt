@file:Suppress("NOTHING_TO_INLINE")
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
package com.google.maps.android.ktx.utils.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.google.maps.android.heatmaps.toWeightedLatLng as canonicalToWeightedLatLng
import com.google.maps.android.heatmaps.heatmapTileProviderWithData as canonicalHeatmapTileProviderWithData
import com.google.maps.android.heatmaps.heatmapTileProviderWithWeightedData as canonicalHeatmapTileProviderWithWeightedData

@Deprecated("Moved to com.google.maps.android.heatmaps.toWeightedLatLng", ReplaceWith("toWeightedLatLng(intensity)", "com.google.maps.android.heatmaps.toWeightedLatLng"))
public inline fun LatLng.toWeightedLatLng(
    intensity: Double = WeightedLatLng.DEFAULT_INTENSITY
): WeightedLatLng = this.canonicalToWeightedLatLng(intensity)

@Deprecated("Moved to com.google.maps.android.heatmaps.heatmapTileProviderWithData", ReplaceWith("heatmapTileProviderWithData(latLngs, radius, gradient, opacity, maxIntensity)", "com.google.maps.android.heatmaps.heatmapTileProviderWithData"))
public inline fun heatmapTileProviderWithData(
    latLngs: Collection<LatLng>,
    radius: Int = HeatmapTileProvider.DEFAULT_RADIUS,
    gradient: Gradient = HeatmapTileProvider.DEFAULT_GRADIENT,
    opacity: Double = HeatmapTileProvider.DEFAULT_OPACITY,
    maxIntensity: Double = 0.0
) : HeatmapTileProvider = canonicalHeatmapTileProviderWithData(latLngs, radius, gradient, opacity, maxIntensity)

@Deprecated("Moved to com.google.maps.android.heatmaps.heatmapTileProviderWithWeightedData", ReplaceWith("heatmapTileProviderWithWeightedData(latLngs, radius, gradient, opacity, maxIntensity)", "com.google.maps.android.heatmaps.heatmapTileProviderWithWeightedData"))
public inline fun heatmapTileProviderWithWeightedData(
    latLngs: Collection<WeightedLatLng>,
    radius: Int = HeatmapTileProvider.DEFAULT_RADIUS,
    gradient: Gradient = HeatmapTileProvider.DEFAULT_GRADIENT,
    opacity: Double = HeatmapTileProvider.DEFAULT_OPACITY,
    maxIntensity: Double = 0.0
) : HeatmapTileProvider = canonicalHeatmapTileProviderWithWeightedData(latLngs, radius, gradient, opacity, maxIntensity)
