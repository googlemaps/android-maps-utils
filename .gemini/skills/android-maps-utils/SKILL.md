---
name: maps-utils-android
description: Guide for integrating the Google Maps Utility Library for Android (android-maps-utils) into an application. Use when users ask to add features like Marker Clustering, Heatmaps, GeoJSON, KML, Polyline encoding/decoding, or Units (distance and elevation conversion and formatting).
---

# Google Maps Utility Library for Android Integration

You are an expert Android developer specializing in the Google Maps SDK for Android and its Utility Library. Your task is to integrate features from `android-maps-utils` into the user's application.

## 1. Setup Dependencies

Add the necessary dependency to the app-level `build.gradle.kts` file:

```kotlin
dependencies {
    // Google Maps Utility Library
    implementation("com.google.maps.android:android-maps-utils:6.1.0") // x-release-please-version
}
```

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

### Units & Logical Distance/Elevation Formatting
Used to represent measurements with the type-safe `Meters` inline value class and format distances and elevations in Metric (`m`, `km`) or Imperial (`ft`, `mi`) units based on the device's `Locale` (`US`, `LR`, `MM`, and `GB` default to `IMPERIAL`; all others default to `METRIC`) or an explicit `UnitSystem` override.

* **Logical Precision Rules:**
  * **Small distances** (`< 1 km` metric or `< 0.25 mi` imperial) and **all elevations** format as whole integers with locale thousands grouping (`"450 m"`, `"528 ft"`, `"14,440 ft"`).
  * **Medium distances** (`1.0` to `< 10` km, or `0.25` to `< 10` mi) format with 1 decimal digit, keeping trailing `.0` (`"0.3 mi"`, `"1.0 km"`, `"2.5 mi"`).
  * **Large distances** (`>= 10` km or `>= 10` mi) round to whole integers (`"10 km"`, `"12 mi"`).

* **Kotlin Example:**
```kotlin
import com.google.maps.android.Meters
import com.google.maps.android.UnitSystem
import com.google.maps.android.feet
import com.google.maps.android.getUnitsConverter
import com.google.maps.android.km
import com.google.maps.android.meters
import com.google.maps.android.miles
import com.google.maps.android.sphericalDistance
import com.google.maps.android.toDistanceString
import com.google.maps.android.toElevationString
import com.google.maps.android.toFeet

// Construct type-safe Meters and perform arithmetic or unit conversions
val summitElevation: Meters = 4401.2.meters
val elevationInFeet: Double = summitElevation.toFeet // 14439.6325...
val routeDistance: Meters = startLatLng.sphericalDistance(endLatLng).meters + 500.feet

// Format using the device's default Locale (no Android Context required)
val defaultDistance: String = routeDistance.toDistanceString()
val defaultElevation: String = summitElevation.toElevationString()

// Or resolve an explicit Metric / Imperial converter
val metric = getUnitsConverter(UnitSystem.METRIC)
val imperial = getUnitsConverter(UnitSystem.IMPERIAL)

val shortWalk = 450.meters.toDistanceString(metric)       // "450 m"
val quarterMile = 0.25.miles.toDistanceString(imperial)   // "0.3 mi"
val mediumDrive = 2.0.km.toDistanceString(metric)         // "2.0 km"
val longTrip = 12.4.km.toDistanceString(metric)           // "12 km"
val summit = summitElevation.toElevationString(imperial)  // "14,440 ft"
```

* **Java Example:**
```java
import static com.google.maps.android.Units.feet;
import static com.google.maps.android.Units.getUnitsConverter;
import static com.google.maps.android.Units.km;
import static com.google.maps.android.Units.meters;
import static com.google.maps.android.Units.miles;
import static com.google.maps.android.Units.toDistanceString;
import static com.google.maps.android.Units.toElevationString;
import static com.google.maps.android.Units.toFeet;

import com.google.maps.android.SphericalUtil;
import com.google.maps.android.UnitSystem;
import com.google.maps.android.UnitsConverter;

// Express measurements in meters and convert between units
double twelveFeetInMeters = feet(12);                     // 3.6576 m
double elevationInFeet = toFeet(meters(4401.2));          // 14439.63 ft

// Format distances and elevations using device default or explicit UnitSystem
double distanceMeters = SphericalUtil.computeDistanceBetween(pointA, pointB);
String defaultFormatted = toDistanceString(distanceMeters);

UnitsConverter imperial = getUnitsConverter(UnitSystem.IMPERIAL);
String quarterMile = imperial.toDistanceString(miles(0.25)); // "0.3 mi"
String mediumDrive = imperial.toDistanceString(km(2.0));     // "1.2 mi"
String summit = imperial.toElevationString(meters(4401.2));  // "14,440 ft"
```

## 3. Best Practices
1. **Performance:** For massive datasets (e.g., parsing huge GeoJSON files), do not block the main thread. Parse on a background dispatcher and only call `layer.addLayerToMap()` on the UI thread.
2. **Custom Renderers:** If the user wants custom cluster icons, extend `DefaultClusterRenderer` and override `onBeforeClusterItemRendered` and `onBeforeClusterRendered`.
