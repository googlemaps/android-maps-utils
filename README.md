[![Maven Central](https://img.shields.io/maven-central/v/com.google.maps.android/android-maps-utils)](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils)
![Release](https://github.com/googlemaps/android-maps-utils/workflows/Release/badge.svg)
![Stable](https://img.shields.io/badge/stability-stable-green)
[![Tests/Build](https://github.com/googlemaps/android-maps-utils/actions/workflows/test.yml/badge.svg)](https://github.com/googlemaps/android-maps-utils/actions/workflows/test.yml)

![Contributors](https://img.shields.io/github/contributors/googlemaps/android-maps-utils?color=green)
[![License](https://img.shields.io/github/license/googlemaps/android-maps-utils?color=blue)][license]
[![StackOverflow](https://img.shields.io/stackexchange/stackoverflow/t/google-maps?color=orange&label=google-maps&logo=stackoverflow)](https://stackoverflow.com/questions/tagged/google-maps)
[![Discord](https://img.shields.io/discord/676948200904589322?color=6A7EC2&logo=discord&logoColor=ffffff)][Discord server]

# Maps SDK for Android Utility Library

## Description

This open-source library contains utilities that are useful for a wide
range of applications using the [Google Maps SDK for Android][maps-sdk].

As of version 5.0.0, the library has been rewritten in Kotlin and split into
focused submodules so you can depend only on what you need. If you are
upgrading from 4.x, see the [migration guide](MIGRATION.md).

- **Marker animation** - animates a marker from one position to another
- **Marker clustering** — handles the display of a large number of points
- **Marker icons** — display text on your Markers
- **Heatmaps** — display a large number of points as a heat map
- **Import KML** — displays KML data on the map
- **Import GeoJSON** — displays and styles GeoJSON data on the map
- **Polyline encoding and decoding** — compact encoding for paths,
  interoperability with Maps API web services
- **Spherical geometry** — for example: computeDistance, computeHeading,
  computeArea
- **Units & logical distance/elevation formatting** — type-safe `Meters` value class, framework-agnostic `UnitSystem` / `UnitsConverter` selection (`MetricUnitsConverter` and `ImperialUnitsConverter`) defaulting to the device's locale, and order-of-magnitude distance formatting
- **Street View metadata** — checks if a Street View panorama exists at a given location
- **Reactive Kotlin Extensions & Builders** — coroutine suspensions (`awaitMap()`), reactive `Flow` observers (`mapClickEvents()`), and option builder DSLs (`addMarker { ... }`) consolidated directly into `com.google.maps.android.*`.

> [!IMPORTANT]
> **KTX Consolidation Notice (`v6.0.0+`)**: All Kotlin extensions (`maps-ktx` and `maps-utils-ktx` from `android-maps-ktx`) are now built directly into `android-maps-utils` under the canonical `com.google.maps.android.*` packages. Separate dependencies on `android-maps-ktx` or `maps-utils-ktx` are no longer needed and should be removed. Legacy calls to `com.google.maps.android.ktx.*` packages remain supported via `@Deprecated(level = DeprecationLevel.WARNING)` bridges that forward directly to canonical implementations.

<p align="center"><img width="90%" vspace="20" src="https://cloud.githubusercontent.com/assets/1950036/6629704/f57bc6d8-c908-11e4-815a-0d909fe02f99.gif"></p>

## Requirements

* Android API level 24+
* [Sign up with Google Maps Platform]
* A Google Maps Platform [project] with the **Maps SDK for Android** enabled
- An [API key] associated with the project above ... follow the [API key instructions] if you're new to the process

## Installation

```kotlin
dependencies {
    // Utilities and consolidated Kotlin Extensions for Maps SDK for Android
    // The aggregator artifact transitively pulls in all submodules below.
    implementation("com.google.maps.android:android-maps-utils:6.1.0") // x-release-please-version
}
```

Alternatively, depend only on the submodules your app needs:

```kotlin
dependencies {
    // Base utilities: PolyUtil, SphericalUtil, collection managers, Street View metadata
    implementation("com.google.maps.android:android-maps-utils-core:6.1.0") // x-release-please-version
    // Marker clustering
    implementation("com.google.maps.android:android-maps-utils-clustering:6.1.0") // x-release-please-version
    // KML and GeoJSON import
    implementation("com.google.maps.android:android-maps-utils-data:6.1.0") // x-release-please-version
    // Heatmaps
    implementation("com.google.maps.android:android-maps-utils-heatmaps:6.1.0") // x-release-please-version
    // Marker icons and animation
    implementation("com.google.maps.android:android-maps-utils-ui:6.1.0") // x-release-please-version
}
```

### Compatibility with Google Navigation SDK for Android

If your project integrates the [Google Navigation SDK for Android](https://developers.google.com/maps/documentation/navigation/android-sdk), you must exclude the transitive `play-services-maps` dependency to prevent duplicate class conflicts:

```kotlin
configurations.all {
    if (name.contains("navigation", ignoreCase = true)) {
        exclude(group = "com.google.android.gms", module = "play-services-maps")
    }
}
```

## Sample App

<img src="https://developers.google.com/maps/documentation/android-sdk/images/utility-markercluster.png" width="150" align=right>

This repository includes a [sample app](demo) that illustrates the use of this library.

To run the demo app, ensure you've met the requirements above then:
1. Clone the repository
1. Add a file `secrets.properties` in the root project (this file should *NOT* be under version control to protect your API key)
1. Add the following keys to `secrets.properties`:
   - `MAPS_API_KEY`: **Required**. Your Google Maps API Key. Ensure it has the **Maps SDK for Android** enabled.
   - `PLACES_API_KEY`: **Optional**. Required for demos using the Places API (e.g., Heatmaps with Places). Ensure the **Places API** is enabled.
   - `MAP_ID`: **Optional**. Required for demos using Advanced Markers or Cloud-based Map Styling.

   For example:
   ```properties
   MAPS_API_KEY=YOUR_MAPS_API_KEY
   PLACES_API_KEY=YOUR_PLACES_API_KEY
   MAP_ID=YOUR_MAP_ID
   ```
1. Build and run the `debug` variant for the Maps SDK for Android version

### Setting up the Map ID

Some of the features in the demo app, such as Advanced Markers, require a Map ID. You can learn more about map IDs in the [official documentation](https://developers.google.com/maps/documentation/android-sdk/map-ids/mapid-over). You can set the Map ID in one of the following ways:

1.  **`secrets.properties`:** Add a line to your `secrets.properties` file with your Map ID:
    ```properties
    MAP_ID=YOUR_MAP_ID
    ```

2.  **`strings.xml`:** Alternatively, you can set the Map ID in the `demo/src/main/res/values/strings.xml` file:
    ```xml
    <string name="map_id">YOUR_MAP_ID</string>
    ```

3.  **XML Layout Files:** You can also hardcode the Map ID directly in the XML layout files where a map is defined, by setting the `map:mapId` attribute:
    ```xml
    <androidx.fragment.app.FragmentContainerView
        map:mapId="YOUR_MAP_ID" 
        />
    ```

## Documentation

See the [documentation] for a full list of classes and their methods.

Full guides for using the utilities are published in
[Google Maps Platform documentation](https://developers.google.com/maps/documentation/android-sdk/utility).

## Usage

<details>
  <summary>Marker utilities</summary>

### Marker utilities

- Marker animation [source](https://github.com/googlemaps/android-maps-utils/blob/main/ui/src/main/java/com/google/maps/android/ui/AnimationUtil.kt), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/AnimationUtilDemoActivity.java)
- Marker clustering [source](https://github.com/googlemaps/android-maps-utils/tree/main/clustering/src/main/java/com/google/maps/android/clustering), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering)
- Advanced Markers clustering [source](https://github.com/googlemaps/android-maps-utils/tree/main/clustering/src/main/java/com/google/maps/android/clustering), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/CustomAdvancedMarkerClusteringDemoActivity.java)
- Marker icons [source](https://github.com/googlemaps/android-maps-utils/blob/main/ui/src/main/java/com/google/maps/android/ui/IconGenerator.kt), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/IconGeneratorDemoActivity.java)
</details>

<details>
  <summary>Data visualization utilities</summary>

### Data visualization utilities

- Display heat maps [source](https://github.com/googlemaps/android-maps-utils/tree/main/heatmaps/src/main/java/com/google/maps/android/heatmaps), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/heatmap)
- Import GeoJSON [source](https://github.com/googlemaps/android-maps-utils/tree/main/data/src/main/java/com/google/maps/android/data/geojson), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/geojson)
- Import KML [source](https://github.com/googlemaps/android-maps-utils/tree/main/data/src/main/java/com/google/maps/android/data/kml), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/kml)

</details>

<details>
  <summary>Polyline and spherical geometry utilities</summary>

### Additional utilities

- Polyline encoding and decoding [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/PolyUtil.kt), [encoding sample](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/PolySimplifyDemoActivity.java), [decoding sample](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/PolyDecodeDemoActivity.java)
- Spherical geometry [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/SphericalUtil.kt), [compute distance sample](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/DistanceDemoActivity.java)
- Units & distance/elevation formatting [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/Units.kt), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/UnitsDemoActivity.kt)

</details>

<details>
  <summary>Units & Logical Distance/Elevation Formatting</summary>

### Units & Logical Distance/Elevation Formatting

The `Units` API (`com.google.maps.android`) provides a type-safe `Meters` value class, locale- and `Resources`-aware `UnitSystem` resolution, and logical significant-digit formatting for distances and elevations:

- **Locale Defaults**:
  - `US`, `LR`, and `MM` default to **Imperial** (`ft`, `mi`) for both road distances and elevations.
  - `GB` (`Locale.UK`) defaults out-of-the-box to **Imperial road distances** (`ft`, `mi`) and **Metric elevations** (`m`).
  - All other locales default to **Metric** (`m`, `km`) for both road distances and elevations.
- **Logical Precision Rules**:
  - **Small distances** (`< 1 km` metric or `< 0.25 mi` imperial) and **all elevations** format as whole integers (`450 m`, `528 ft`, `14,440 ft`).
  - **Medium distances** (`1.0` to `< 10` km, or `0.25` to `< 10` mi) format with 1 decimal digit, keeping trailing `.0` (`0.3 mi`, `1.0 km`, `2.5 mi`).
  - **Large distances** (`>= 10` km or `>= 10` mi) round to whole numbers (`10 km`, `12 mi`).

```kotlin
import com.google.maps.android.ImperialUnitsConverter
import com.google.maps.android.MetricUnitsConverter
import com.google.maps.android.UnitSystem
import com.google.maps.android.UnitsConverter
import com.google.maps.android.getUnitsConverter
import com.google.maps.android.inFeet
import com.google.maps.android.inKilometers
import com.google.maps.android.kilometers
import com.google.maps.android.meters
import com.google.maps.android.miles
import com.google.maps.android.toDistanceString
import com.google.maps.android.toElevationString
import java.util.Locale

// 1. Default formatting using device Locale, explicit Locale, or Android Resources (for localized unit templates)
val distanceText = 450.meters.toDistanceString()                       // Uses Locale.getDefault()
val localizedText = 450.meters.toDistanceString(context.resources)     // Uses localized R.string.amu_in_* templates
val ukDistance = 1.miles.toDistanceString(Locale.UK)                   // "1.0 mi"
val ukElevation = 1345.meters.toElevationString(Locale.UK)             // "1,345 m" (GB defaults to miles/ft + metres)

// 2. Override both distance and elevation to Metric or Imperial
val imperial = getUnitsConverter(UnitSystem.IMPERIAL)
val metric = getUnitsConverter(UnitSystem.METRIC)

val summitFeet = 4401.2.meters.inFeet                                  // 14439.6325...
val shortWalk = 450.meters.toDistanceString(metric)                    // "450 m"
val quarterMile = 0.25.miles.toDistanceString(imperial)                // "0.3 mi"
val mediumDrive = 2.0.kilometers.toDistanceString(metric)              // "2.0 km"
val longTrip = 12.4.kilometers.toDistanceString(metric)                // "12 km"
val summit = 4401.2.meters.toElevationString(imperial)                 // "14,440 ft"

// 3. Override only distance or only elevation (via factory parameters, fluent copy methods, or UnitSystem pair)
val usMetricElevation = getUnitsConverter(Locale.US, elevationUnits = UnitSystem.METRIC)
val ukImperialElevation = getUnitsConverter(Locale.UK).withElevationUnits(UnitSystem.IMPERIAL)
val hybridBySystem = UnitsConverter(
    distanceUnits = UnitSystem.IMPERIAL,
    elevationUnits = UnitSystem.METRIC,
)

// 4. Mix & match converters or supply custom MeasurementFormatter lambdas
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

In Java, `Units` exposes static conversion and formatting methods (usable with `import static com.google.maps.android.Units.*;`):

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
import com.google.maps.android.UnitSystem;
import com.google.maps.android.UnitsConverter;
import java.util.Locale;

double twelveFeetInMeters = feet(12);                                       // 3.6576 m
double elevationInFeet = inFeet(meters(4401.2));                            // 14439.63 ft

// 1. Default formatting (Locale or Android Resources)
String defaultDistance = toDistanceString(miles(2.5));                      // "2.5 mi" (in US/GB locale)
String localizedDistance = toDistanceString(miles(2.5), context.getResources());
String ukElevation = toElevationString(meters(1345), Locale.UK);            // "1,345 m"

// 2. Override both distance and elevation
UnitsConverter imperial = getUnitsConverter(UnitSystem.IMPERIAL);
String distance = imperial.toDistanceString(miles(2.5));                    // "2.5 mi"
String summit = imperial.toElevationString(meters(4401.2));                 // "14,440 ft"

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

</details>

<details>
  <summary>Reactive Kotlin Extensions & Builders (Consolidated in v6.0.0)</summary>

### Reactive Kotlin Extensions & Builders

All Kotlin extensions formerly provided by `android-maps-ktx` (`maps-ktx` and `maps-utils-ktx`) are now integrated into `android-maps-utils` (`v6.0.0+`) under canonical packages (`com.google.maps.android.*`, `com.google.maps.android.clustering.*`, etc.).

#### 1. Coroutine Suspensions (`awaitMapsSdkInitialized()`, `awaitMap()`, `awaitAnimateCamera()`)
```kotlin
import com.google.android.gms.maps.MapsInitializer
import com.google.maps.android.awaitMapsSdkInitialized
import com.google.maps.android.awaitMap
import com.google.maps.android.awaitAnimateCamera

// Suspend until Maps SDK is initialized
val renderer: MapsInitializer.Renderer = context.awaitMapsSdkInitialized(MapsInitializer.Renderer.LATEST)

// Suspend until GoogleMap is ready on MapView / MapFragment
val googleMap: GoogleMap = mapView.awaitMap()

// Suspend until camera animation completes
googleMap.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12f))
```

#### 2. Option Builders DSL (`addMarker`, `addPolyline`, `addPolygon`)
```kotlin
import com.google.maps.android.addMarker
import com.google.maps.android.addCircle

googleMap.addMarker {
    position(LatLng(-33.852, 151.211))
    title("Sydney Opera House")
}

googleMap.addCircle {
    center(LatLng(-33.870, 151.200))
    radius(500.0)
    strokeWidth(2f)
}
```

#### 3. Reactive `Flow` Observers (`mapClickEvents`, `cameraMoveEvents`)
```kotlin
import com.google.maps.android.mapClickEvents

lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        googleMap.mapClickEvents().collect { latLng ->
            Log.d("MapClick", "Clicked: $latLng")
        }
    }
}
```

#### Backward Compatibility & Deprecation
Existing references to `com.google.maps.android.ktx.*` continue to work through `@Deprecated(level = DeprecationLevel.WARNING)` forwarding wrappers. You can safely migrate your code incrementally to `com.google.maps.android.*`.

</details>

<details>
  <summary>Street View metadata utility</summary>

### Street View metadata utility

The StreetViewUtil class provides functionality to check whether a location is supported in StreetView. You can avoid errors when [adding a Street View panorama](https://developers.google.com/maps/documentation/android-sdk/streetview) to an Android app by calling this metadata utility and only adding a Street View panorama if the response is `OK`.

```kotlin
StreetViewUtils.fetchStreetViewData(LatLng(8.1425918, 11.5386121), BuildConfig.MAPS_API_KEY,Source.DEFAULT)
```

`fetchStreetViewData` will return `NOT_FOUND`, `OK`, `ZERO_RESULTS` or `REQUEST_DENIED`, depending on the response.

By default, the `Source` is set to `Source.DEFAULT`, but you can also specify `Source.OUTDOOR` to request outdoor Street View panoramas.

</details>

## Internal usage attribution ID

This library calls the `addInternalUsageAttributionId` method, which helps Google understand which libraries and samples are helpful to developers and is optional. Instructions for opting out of the identifier are provided below.

If you wish to disable this, you can do so by removing the initializer in your `AndroidManifest.xml` using the `tools:node="remove"` attribute:

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="com.google.maps.android.utils.attribution.AttributionIdInitializer"
        tools:node="remove" />
</provider>
```

## Contributing

Contributions are welcome and encouraged! If you'd like to contribute, send us a [pull request] and refer to our [code of conduct] and [contributing guide].

### Using AI
This repository provides an official Gemini Skill and an `llm-integration-prompt.md` to help AI agents navigate the codebase and provide assistance. Refer to the [contributing guide] for more details on AI usage.


## Terms of Service

This library uses Google Maps Platform services. Use of Google Maps Platform services through this library is subject to the Google Maps Platform [Terms of Service].

If your billing address is in the European Economic Area, effective on 8 July 2025, the [Google Maps Platform EEA Terms of Service](https://cloud.google.com/terms/maps-platform/eea) will apply to your use of the Services. Functionality varies by region. [Learn more](https://developers.google.com/maps/comms/eea/faq).

This library is not a Google Maps Platform Core Service. Therefore, the Google Maps Platform Terms of Service (e.g. Technical Support Services, Service Level Agreements, and Deprecation Policy) do not apply to the code in this library.

## Support

This library is offered via an open source [license]. It is not governed by the Google Maps Platform Support [Technical Support Services Guidelines], the [SLA], or the [Deprecation Policy]. However, any Google Maps Platform services used by the library remain subject to the Google Maps Platform Terms of Service.

This library adheres to [semantic versioning] to indicate when backwards-incompatible changes are introduced. Accordingly, while the library is in version 0.x, backwards-incompatible changes may be introduced at any time.

If you find a bug, or have a feature request, please [file an issue] on GitHub. If you would like to get answers to technical questions from other Google Maps Platform developers, ask through one of our [developer community channels]. If you'd like to contribute, please check the [contributing guide].

You can also discuss this library on our [Discord server].

[API key]: https://developers.google.com/maps/documentation/android-sdk/get-api-key
[API key instructions]: https://developers.google.com/maps/documentation/android-sdk/config#step_3_add_your_api_key_to_the_project
[maps-sdk]: https://developers.google.com/maps/documentation/android-sdk
[documentation]: https://googlemaps.github.io/android-maps-utils
[android-maps-ktx]: https://github.com/googlemaps/android-maps-ktx

[code of conduct]: ?tab=coc-ov-file#readme
[contributing guide]: CONTRIBUTING.md
[Deprecation Policy]: https://cloud.google.com/maps-platform/terms
[developer community channels]: https://developers.google.com/maps/developer-community
[Discord server]: https://discord.gg/hYsWbmk
[file an issue]: https://github.com/googlemaps/android-maps-utils/issues/new/choose
[license]: LICENSE
[project]: https://developers.google.com/maps/documentation/android-sdk/cloud-setup
[pull request]: https://github.com/googlemaps/android-maps-utils/compare
[semantic versioning]: https://semver.org
[Sign up with Google Maps Platform]: https://console.cloud.google.com/google/maps-apis/start
[similar inquiry]: https://github.com/googlemaps/android-maps-utils/issues
[SLA]: https://cloud.google.com/maps-platform/terms/sla
[Technical Support Services Guidelines]: https://cloud.google.com/maps-platform/terms/tssg
[Terms of Service]: https://cloud.google.com/maps-platform/terms

