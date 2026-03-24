---
name: maps-utils-android
description: Guide for integrating the Google Maps Utility Library for Android (android-maps-utils) into an application. Use when users ask to add features like Marker Clustering, Heatmaps, GeoJSON, KML, or Polyline encoding/decoding.
---

# Google Maps Utility Library for Android Integration

You are an expert Android developer specializing in the Google Maps SDK for Android and its Utility Library. Your task is to integrate features from `android-maps-utils` into the user's application.

## 1. Setup Dependencies

Add the necessary dependency to the app-level `build.gradle.kts` file:

```kotlin
dependencies {
    // Google Maps Utility Library
    implementation("com.google.maps.android:android-maps-utils:3.9.0") // Check for the latest version
}
```
*(Note: If the user is using Jetpack Compose, they should ideally be using `maps-compose-utils` from the `android-maps-compose` repository instead of this library directly, though this library is the underlying foundation).*

## 2. Core Features & Usage Patterns

When a user asks for advanced features, implement them using these established patterns from the Utility Library:

### Marker Clustering
Used to manage multiple markers at different zoom levels.
1. Create a `ClusterItem` data class.
2. Initialize a `ClusterManager` inside `onMapReady`.
3. Point the map's `setOnCameraIdleListener` and `setOnMarkerClickListener` to the `ClusterManager`.
4. Add items using `clusterManager.addItem()`.

### Heatmaps
Used to represent the density of data points.
1. Provide a `Collection<LatLng>` or `Collection<WeightedLatLng>`.
2. Create a `HeatmapTileProvider` with the builder `HeatmapTileProvider.Builder().data(list).build()`.
3. Add the overlay to the map: `map.addTileOverlay(TileOverlayOptions().tileProvider(provider))`.

### GeoJSON and KML
Used to import geographic data from external files.
* **GeoJSON:** `val layer = GeoJsonLayer(map, R.raw.geojson_file, context); layer.addLayerToMap()`
* **KML:** `val layer = KmlLayer(map, R.raw.kml_file, context); layer.addLayerToMap()`

### Polyline Decoding and Spherical Geometry
Used for server-client coordinate compression and distance calculations.
* **Decoding:** `PolyUtil.decode(encodedPathString)`
* **Distance:** `SphericalUtil.computeDistanceBetween(latLng1, latLng2)`

## 3. Best Practices
1. **Performance:** For massive datasets (e.g., parsing huge GeoJSON files), do not block the main thread. Parse on a background dispatcher and only call `layer.addLayerToMap()` on the UI thread.
2. **Custom Renderers:** If the user wants custom cluster icons, extend `DefaultClusterRenderer` and override `onBeforeClusterItemRendered` and `onBeforeClusterRendered`.
