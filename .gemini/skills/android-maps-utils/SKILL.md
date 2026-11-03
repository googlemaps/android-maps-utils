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
Used to represent measurements with the type-safe `Meters` inline value class and format distances and elevations in Metric (`m`, `km`) or Imperial (`ft`, `mi`) units based on the device's `Locale` or `Resources` (`US`, `LR`, and `MM` default to `IMPERIAL` for both; `GB` defaults to `IMPERIAL` road distances + `METRIC` elevations; all others default to `METRIC`), or an explicit `UnitSystem` / formatter override.

* **Logical Precision Rules:**
  * **Small distances** (`< 1 km` metric or `< 0.25 mi` imperial) and **all elevations** format as whole integers with locale thousands grouping (`"450 m"`, `"528 ft"`, `"14,440 ft"`).
  * **Medium distances** (`1.0` to `< 10` km, or `0.25` to `< 10` mi) format with 1 decimal digit, keeping trailing `.0` (`"0.3 mi"`, `"1.0 km"`, `"2.5 mi"`).
  * **Large distances** (`>= 10` km or `>= 10` mi) round to whole integers (`"10 km"`, `"12 mi"`).

* **Kotlin Example:**
```kotlin
import com.google.maps.android.ImperialUnitsConverter
import com.google.maps.android.Meters
import com.google.maps.android.MetricUnitsConverter
import com.google.maps.android.UnitSystem
import com.google.maps.android.UnitsConverter
import com.google.maps.android.feet
import com.google.maps.android.getUnitsConverter
import com.google.maps.android.inFeet
import com.google.maps.android.inKilometers
import com.google.maps.android.kilometers
import com.google.maps.android.meters
import com.google.maps.android.miles
import com.google.maps.android.sphericalDistance
import com.google.maps.android.toDistanceString
import com.google.maps.android.toElevationString
import java.util.Locale

// Construct type-safe Meters and perform arithmetic or unit conversions
val summitElevation: Meters = 4401.2.meters
val elevationInFeet: Double = summitElevation.inFeet // 14439.6325...
val routeDistance: Meters = startLatLng.sphericalDistance(endLatLng).meters + 500.feet

// 1. Default formatting using device Locale, explicit Locale, or Android Resources
val defaultDistance: String = routeDistance.toDistanceString()
val localizedDistance: String = routeDistance.toDistanceString(context.resources)
val ukDistance: String = 1.miles.toDistanceString(Locale.UK)           // "1.0 mi"
val ukElevation: String = 1345.meters.toElevationString(Locale.UK)     // "1,345 m"

// 2. Override both distance and elevation to Metric or Imperial
val metric = getUnitsConverter(UnitSystem.METRIC)
val imperial = getUnitsConverter(UnitSystem.IMPERIAL)

val shortWalk = 450.meters.toDistanceString(metric)                    // "450 m"
val quarterMile = 0.25.miles.toDistanceString(imperial)                // "0.3 mi"
val mediumDrive = 2.0.kilometers.toDistanceString(metric)              // "2.0 km"
val longTrip = 12.4.kilometers.toDistanceString(metric)                // "12 km"
val summit = summitElevation.toElevationString(imperial)               // "14,440 ft"

// 3. Override only distance or only elevation
val usMetricElevation = getUnitsConverter(Locale.US, elevationUnits = UnitSystem.METRIC)
val ukImperialElevation = getUnitsConverter(Locale.UK).withElevationUnits(UnitSystem.IMPERIAL)
val hybridBySystem = UnitsConverter(
    distanceUnits = UnitSystem.IMPERIAL,
    elevationUnits = UnitSystem.METRIC,
)

// 4. Compact formatting & custom unit thresholds (e.g., for a map ScaleBar widget)
val scaleBarImperial = ImperialUnitsConverter(milesThreshold = 1.miles)
val compactMetricLabel = 1000.meters.toDistanceString(MetricUnitsConverter, NumberFormatSymbols.COMPACT)   // "1 km"
val compactImperialLabel = 500.meters.toDistanceString(scaleBarImperial, NumberFormatSymbols.COMPACT)      // "1640 ft"

// 5. Mix & match converters or supply custom MeasurementFormatter lambdas
val customConverter = UnitsConverter(
    distanceFormatter = { meters, symbols ->
        "${symbols.formatFixedDecimal(meters.inKilometers, decimals = 2)} km"
    },
    elevationConverter = MetricUnitsConverter,
)
val customElevation = getUnitsConverter().withElevationFormatter { meters, symbols ->
    "${symbols.formatInteger(meters.value)} MASL"
}
```

* **Java Example:**
```java
import static com.google.maps.android.Units.feet;
import static com.google.maps.android.Units.getUnitsConverter;
import static com.google.maps.android.Units.inFeet;
import static com.google.maps.android.Units.inKilometers;
import static com.google.maps.android.Units.kilometers;
import static com.google.maps.android.Units.meters;
import static com.google.maps.android.Units.miles;
import static com.google.maps.android.Units.toDistanceString;
import static com.google.maps.android.Units.toElevationString;

import com.google.maps.android.ImperialUnitsConverter;
import com.google.maps.android.MetricUnitsConverter;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.UnitSystem;
import com.google.maps.android.UnitsConverter;
import java.util.Locale;

// Express measurements in meters and convert between units
double twelveFeetInMeters = feet(12);                                      // 3.6576 m
double elevationInFeet = inFeet(meters(4401.2));                           // 14439.63 ft

// 1. Default formatting (Locale or Android Resources)
double distanceMeters = SphericalUtil.computeDistanceBetween(pointA, pointB);
String defaultFormatted = toDistanceString(distanceMeters);
String localizedFormatted = toDistanceString(distanceMeters, context.getResources());
String ukElevation = toElevationString(meters(1345), Locale.UK);           // "1,345 m"

// 2. Override both distance and elevation
UnitsConverter imperial = getUnitsConverter(UnitSystem.IMPERIAL);
String quarterMile = imperial.toDistanceString(miles(0.25));               // "0.3 mi"
String mediumDrive = imperial.toDistanceString(kilometers(2.0));           // "1.2 mi"
String summit = imperial.toElevationString(meters(4401.2));                // "14,440 ft"

// 3. Override only distance or only elevation
UnitsConverter usMetricElevation = getUnitsConverter(Locale.US)
    .withElevationUnits(UnitSystem.METRIC);
UnitsConverter hybridBySystem = UnitsConverter.create(
    UnitSystem.IMPERIAL,
    UnitSystem.METRIC
);

// 4. Mix & match converters or supply custom MeasurementFormatter lambdas
UnitsConverter ukConverter = UnitsConverter.create(
    ImperialUnitsConverter.INSTANCE,
    MetricUnitsConverter.INSTANCE
);
UnitsConverter customConverter = UnitsConverter.create(
    (meters, symbols) -> symbols.formatFixedDecimal(inKilometers(meters), 2) + " km",
    MetricUnitsConverter.INSTANCE::toElevationString
);
```

## 3. Best Practices
1. **Performance:** For massive datasets (e.g., parsing huge GeoJSON files), do not block the main thread. Parse on a background dispatcher and only call `layer.addLayerToMap()` on the UI thread.
2. **Custom Renderers:** If the user wants custom cluster icons, extend `DefaultClusterRenderer` and override `onBeforeClusterItemRendered` and `onBeforeClusterRendered`.
