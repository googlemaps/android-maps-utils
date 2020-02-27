package com.google.maps.ktx.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

/**
 * Converts this LatLng to a [WeightedLatLng]
 */
inline fun LatLng.toWeightedLatLng(intensity: Double = WeightedLatLng.DEFAULT_INTENSITY) =
    WeightedLatLng(this, intensity)

/**
 * Constructs a [HeatmapTileProvider].
 *
 * @throws IllegalStateException when [opacity] is not within the range [0, 1] or if [latLngs] is
 * empty
 */
inline fun heatmapTileProviderWithData(
    latLngs: Collection<LatLng>,
    radius: Int = HeatmapTileProvider.DEFAULT_RADIUS,
    gradient: Gradient = HeatmapTileProvider.DEFAULT_GRADIENT,
    opacity: Double = HeatmapTileProvider.DEFAULT_OPACITY,
    maxIntensity: Double = 0.0
): HeatmapTileProvider {
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
inline fun heatmapTileProviderWithWeightedData(
    latLngs: Collection<WeightedLatLng>,
    radius: Int = HeatmapTileProvider.DEFAULT_RADIUS,
    gradient: Gradient = HeatmapTileProvider.DEFAULT_GRADIENT,
    opacity: Double = HeatmapTileProvider.DEFAULT_OPACITY,
    maxIntensity: Double = 0.0
): HeatmapTileProvider {
    return HeatmapTileProvider.Builder()
        .weightedData(latLngs)
        .radius(radius)
        .gradient(gradient)
        .opacity(opacity)
        .maxIntensity(maxIntensity)
        .build()
}
