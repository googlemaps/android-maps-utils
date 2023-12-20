![Build Status](https://github.com/googlemaps/android-maps-utils/actions/workflows/test.yml/badge.svg?branch=main)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils)
![GitHub contributors](https://img.shields.io/github/contributors/googlemaps/android-maps-utils?color=green)
[![Discord](https://img.shields.io/discord/676948200904589322)](https://discord.gg/hYsWbmk)
![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)

# Maps SDK for Android Utility Library

## Description

This open-source library contains utilities that are useful for a wide
range of applications using the [Google Maps SDK for Android][android-site].

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
- **Street View metadata** — checks if a Street View panorama exists at a given location

You can also find Kotlin extensions for this library in [Maps Android KTX][android-maps-ktx].

<p align="center"><img width="90%" vspace="20" src="https://cloud.githubusercontent.com/assets/1950036/6629704/f57bc6d8-c908-11e4-815a-0d909fe02f99.gif"></p>

## Requirements

* Android API level 21+
- An [API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

## Installation

```groovy
dependencies {
    // Utilities for Maps SDK for Android (requires Google Play Services)
    // You do not need to add a separate dependency for the Maps SDK for Android
    // since this library builds in the compatible version of the Maps SDK.
    implementation 'com.google.maps.android:android-maps-utils:3.8.2'

    // Optionally add the Kotlin Extensions (KTX) for full Kotlin language support
    // See latest version at https://github.com/googlemaps/android-maps-ktx
    // implementation 'com.google.maps.android:maps-utils-ktx:<latest-version>'
}
```

## Demo App

<img src="https://developers.google.com/maps/documentation/android-sdk/images/utility-markercluster.png" width="150" align=right>

This repository includes a [sample app](demo) that illustrates the use of this library.

To run the demo app, you'll have to:

1. [Get a Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
1. Add a file `local.properties` in the root project (this file should *NOT* be under version control to protect your API key)
1. Add a single line to `local.properties` that looks like `MAPS_API_KEY=YOUR_API_KEY`, where `YOUR_API_KEY` is the API key you obtained in the first step
1. Build and run the `debug` variant for the Maps SDK for Android version

## Documentation

See the [reference documentation][dokka] for a full list of classes and their methods.

## Usage

Full guides for using the utilities are published in
[Google Maps Platform documentation][devsite-guide].

<details>
  <summary>Marker utilities</summary>

### Marker utilities

- Marker animation [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/ui/AnimationUtil.java), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/AnimationUtilDemoActivity.java)
- Marker clustering [source](https://github.com/googlemaps/android-maps-utils/tree/main/library/src/main/java/com/google/maps/android/clustering), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/marker-clustering)
- Advanced Markers clustering [source](https://github.com/googlemaps/android-maps-utils/tree/main/library/src/main/java/com/google/maps/android/clustering), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/CustomAdvancedMarkerClusteringDemoActivity.java)
- Marker icons [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/ui/IconGenerator.java), [sample code](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/IconGeneratorDemoActivity.java)

</details>

<details>
  <summary>Data visualization utilities</summary>

### Data visualization utilities

- Display heat maps [source](https://github.com/googlemaps/android-maps-utils/tree/main/library/src/main/java/com/google/maps/android/heatmaps), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/heatmap)
- Import GeoJSON [source](https://github.com/googlemaps/android-maps-utils/tree/main/library/src/main/java/com/google/maps/android/data/geojson), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/geojson)
- Import KML [source](https://github.com/googlemaps/android-maps-utils/tree/main/library/src/main/java/com/google/maps/android/data/kml), [guide](https://developers.google.com/maps/documentation/android-sdk/utility/kml)

</details>

<details>
  <summary>Polyline and spherical geometry utilities</summary>

### Additional utilities

- Polyline encoding and decoding [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/PolyUtil.java), [encoding sample](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/PolySimplifyDemoActivity.java), [decoding sample](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/PolyDecodeDemoActivity.java)
- Spherical geometry [source](https://github.com/googlemaps/android-maps-utils/blob/main/library/src/main/java/com/google/maps/android/SphericalUtil.java), [compute distance sample](https://github.com/googlemaps/android-maps-utils/blob/main/demo/src/main/java/com/google/maps/android/utils/demo/DistanceDemoActivity.java)

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

<details>
  <summary>Migration Guide from v0.x to 1.0</summary>

### Migrating from v0.x to 1.0

Improvements made in version [1.0.0](https://github.com/googlemaps/android-maps-utils/releases/tag/1.0.0) of the library to support multiple layers on the map caused breaking changes to versions prior to it. These changes also modify behaviors that are documented in the [Maps SDK for Android Maps documentation](https://developers.google.com/maps/documentation/android-sdk/intro) site. This section outlines all those changes and how you can migrate to use this library since version 1.0.0.


### Adding Click Events

Click events originate in the layer-specific object that added the marker/ground overlay/polyline/polygon. In each layer, the click handlers are passed to the marker, ground overlay, polyline, or polygon `Collection` object.

```java
// Clustering
ClusterManager<ClusterItem> clusterManager = // Initialize ClusterManager - if you're using multiple maps features, use the constructor that passes in Manager objects (see next section)
clusterManager.setOnClusterItemClickListener(item -> {
    // Listen for clicks on a cluster item here
    return false;
});
clusterManager.setOnClusterClickListener(item -> {
    // Listen for clicks on a cluster here
    return false;
});

// GeoJson
GeoJsonLayer geoJsonLayer = // Initialize GeoJsonLayer - if you're using multiple maps features, use the constructor that passes in Manager objects (see next section)
geoJsonLayer.setOnFeatureClickListener(feature -> {
    // Listen for clicks on GeoJson features here
});

// KML
KmlLayer kmlLayer = // Initialize KmlLayer - if you're using multiple maps features, use the constructor that passes in Manager objects (see next section)
kmlLayer.setOnFeatureClickListener(feature -> {
    // Listen for clicks on KML features here
});
```

#### Using Manager Objects

If you use one of Manager objects in the package `com.google.maps.android` (e.g. `GroundOverlayManager`, `MarkerManager`, etc.), say from adding a KML layer, GeoJson layer, or Clustering, you will have to rely on the Collection specific to add an object to the map rather than adding that object directly to `GoogleMap`. This is because each Manager sets itself as a click listener so that it can manage click events coming from multiple layers.

For example, if you have additional `GroundOverlay` objects:

_New_

```java
GroundOverlayManager groundOverlayManager = // Initialize

// Create a new collection first
GroundOverlayManager.Collection groundOverlayCollection = groundOverlayManager.newCollection();

// Add a new ground overlay
GroundOverlayOptions options = // ...
groundOverlayCollection.addGroundOverlay(options);
```

_Old_

```java
GroundOverlayOptions options = // ...
googleMap.addGroundOverlay(options);
```

This same pattern applies for `Marker`, `Circle`, `Polyline`, and `Polygon`.

### Adding a Custom Info Window
If you use `MarkerManager`, adding an `InfoWindowAdapter` and/or an `OnInfoWindowClickListener` should be done on the `MarkerManager.Collection` object.

_New_
```java
CustomInfoWindowAdapter adapter = // ...
OnInfoWindowClickListener listener = // ...

// Create a new Collection from a MarkerManager
MarkerManager markerManager = // ...
MarkerManager.Collection collection = markerManager.newCollection();

// Set InfoWindowAdapter and OnInfoWindowClickListener
collection.setInfoWindowAdapter(adapter);
collection.setOnInfoWindowClickListener(listener);

// Alternatively, if you are using clustering
ClusterManager<ClusterItem> clusterManager = // ...
MarkerManager.Collection markerCollection = clusterManager.getMarkerCollection();
markerCollection.setInfoWindowAdapter(adapter);
markerCollection.setOnInfoWindowClickListener(listener);
```

_Old_
```java
CustomInfoWindowAdapter adapter = // ...
OnInfoWindowClickListener listener = // ...
googleMap.setInfoWindowAdapter(adapter);
googleMap.setOnInfoWindowClickListener(listener);
```

### Adding a Marker Drag Listener

If you use `MarkerManager`, adding an `OnMarkerDragListener` should be done on the `MarkerManager.Collection` object.

_New_
```java
// Create a new Collection from a MarkerManager
MarkerManager markerManager = // ...
MarkerManager.Collection collection = markerManager.newCollection();

// Add markers to collection
MarkerOptions markerOptions = // ...
collection.addMarker(markerOptions);
// ...

// Set OnMarkerDragListener
GoogleMap.OnMarkerDragListener listener = // ...
collection.setOnMarkerDragListener(listener);

// Alternatively, if you are using clustering
ClusterManager<ClusterItem> clusterManager = // ...
MarkerManager.Collection markerCollection = clusterManager.getMarkerCollection();
markerCollection.setOnMarkerDragListener(listener);
```

_Old_
```java
// Add markers
MarkerOptions markerOptions = // ...
googleMap.addMarker(makerOptions);

// Add listener
GoogleMap.OnMarkerDragListener listener = // ...
googleMap.setOnMarkerDragListener(listener);
```

### Clustering

[A bug](https://github.com/googlemaps/android-maps-utils/issues/90) was fixed in v1 to properly clear and re-add markers via the `ClusterManager`.

For example, this didn't work pre-v1, but works for v1 and later:

```java
clusterManager.clearItems();
clusterManager.addItems(items);
clusterManager.cluster();
```

If you're using custom clustering (i.e, if you're extending `DefaultClusterRenderer`), you must override two additional methods in v1:
*  `onClusterItemUpdated()` - should be the same* as your `onBeforeClusterItemRendered()` method
*  `onClusterUpdated()` - should be the same* as your `onBeforeClusterRendered()` method

**Note that these methods can't be identical, as you need to use a `Marker` instead of `MarkerOptions`*

See the [`CustomMarkerClusteringDemoActivity`](demo/src/gms/java/com/google/maps/android/utils/demo/CustomMarkerClusteringDemoActivity.java) in the demo app for a complete example.

_New_

```java
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        ...
        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person - show their profile photo and set the info window to show their name
            markerOptions
                    .icon(getItemIcon(person))
                    .title(person.name);
        }

        /**
         * New in v1
         */
        @Override
        protected void onClusterItemUpdated(Person person, Marker marker) {
            // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
            marker.setIcon(getItemIcon(person));
            marker.setTitle(person.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            markerOptions.icon(getClusterIcon(cluster));
        }

        /**
         * New in v1
         */
        @Override
        protected void onClusterUpdated(Cluster<Person> cluster, Marker marker) {
            // Same implementation as onBeforeClusterRendered() (to update cached markers)
            marker.setIcon(getClusterIcon(cluster));
        }
        ...
    }
```

_Old_

```java
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        ...
        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person - show their profile photo and set the info window to show their name
            markerOptions
                    .icon(getItemIcon(person))
                    .title(person.name);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            markerOptions.icon(getClusterIcon(cluster));
        }
        ...
    }
```

</details>

## Contributing

Contributions are welcome and encouraged! See the [contributing guide](CONTRIBUTING.md) for more info.

## Support

This library is offered via an open source [license](LICENSE). It is not governed by the Google Maps Platform [Technical Support Services Guidelines](https://cloud.google.com/maps-platform/terms/tssg?utm_source=github&utm_medium=documentation&utm_campaign=&utm_content=web_components), the [SLA](https://cloud.google.com/maps-platform/terms/sla?utm_source=github&utm_medium=documentation&utm_campaign=&utm_content=web_components), or the [Deprecation Policy](https://cloud.google.com/maps-platform/terms?utm_source=github&utm_medium=documentation&utm_campaign=&utm_content=web_components) (however, any Google Maps Platform services used by the library remain subject to the Google Maps Platform Terms of Service).

This library adheres to [semantic versioning](https://semver.org/) to indicate when backwards-incompatible changes are introduced.

If you find a bug, or have a feature request, please [file an issue] on GitHub.

If you would like to get answers to technical questions from other Google Maps Platform developers, ask through one of our [developer community channels](https://developers.google.com/maps/developer-community?utm_source=github&utm_medium=documentation&utm_campaign=&utm_content=web_components) including the Google Maps Platform [Discord server].

[file an issue]: https://github.com/googlemaps/android-maps-utils/issues/new/choose
[pull request]: https://github.com/googlemaps/android-maps-utils/compare
[code of conduct]: CODE_OF_CONDUCT.md
[Discord server]: https://discord.gg/hYsWbmk
[android-site]: https://developers.google.com/maps/documentation/android-sdk
[devsite-guide]: https://developers.google.com/maps/documentation/android-sdk/utility
[dokka]: https://googlemaps.github.io/android-maps-utils/
[android-maps-ktx]: https://github.com/googlemaps/android-maps-ktx
