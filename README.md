![Build Status](https://github.com/googlemaps/android-maps-utils/actions/workflows/test.yml/badge.svg?branch=main)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils)
![GitHub contributors](https://img.shields.io/github/contributors/googlemaps/android-maps-utils?color=green)
[![Discord](https://img.shields.io/discord/676948200904589322)](https://discord.gg/hYsWbmk)
![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)

# Maps SDK for Android Utility Library

## Description

This open-source library contains utilities that are useful for a wide
range of applications using the [Google Maps SDK for Android][android-site].

- **Marker clustering** — handles the display of a large number of points
- **Marker animation** - animates a marker from one position to another
- **Heat maps** — display a large number of points as a heat map
- **IconGenerator** — display text on your Markers
- **Poly decoding and encoding** — compact encoding for paths,
  interoperability with Maps API web services
- **Spherical geometry** — for example: computeDistance, computeHeading,
  computeArea
- **KML** — displays KML data
- **GeoJSON** — displays and styles GeoJSON data

<p align="center"><img width="90%" vspace="20" src="https://cloud.githubusercontent.com/assets/1950036/6629704/f57bc6d8-c908-11e4-815a-0d909fe02f99.gif"></p>

You can also find Kotlin extensions for this library [here][android-maps-ktx].

## Developer Documentation

The generated [reference docs][javadoc] for a full list of classes and their methods.

Written guides for using the utilities are published in
[Google Maps Platform documentation][devsite-guide].

## Requirements

* Android API level 15+
* [Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/config) via Google Play Services

## Installation

```groovy
dependencies {
    // Utilities for Maps SDK for Android (requires Google Play Services)
    implementation 'com.google.maps.android:android-maps-utils:3.4.0'
}
```

## Demo App

<img src="https://developers.google.com/maps/documentation/android-sdk/images/utility-markercluster.png" width="150" align=right>

This repository includes a [demo app](demo) that illustrates the use of this library.

To run the demo app, you'll have to:

1. [Get a Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
1. Open the file `local.properties` in the root project (this file should *NOT* be under version control to protect your API key)
1. Add a single line to `local.properties` that looks like `MAPS_API_KEY=YOUR_API_KEY`, where `YOUR_API_KEY` is the API key you obtained in the first step
1. Build and run the `debug` variant for the Maps SDK for Android version

<details>
  <summary><strong>Migration Guide from v0.x to 1.0</strong></summary>

## Migration Guide from v0.x to 1.0

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

## Support

Encounter an issue while using this library?

If you find a bug or have a feature request, please [file an issue].
Or, if you'd like to contribute, send us a [pull request] and refer to our [code of conduct].

You can also reach us on our [Discord channel].

For more information, check out the detailed guide on the
[Google Developers site][devsite-guide].

[file an issue]: https://github.com/googlemaps/android-maps-utils/issues/new/choose
[pull request]: https://github.com/googlemaps/android-maps-utils/compare
[code of conduct]: CODE_OF_CONDUCT.md
[Discord channel]: https://discord.gg/hYsWbmk
[android-site]: https://developers.google.com/maps/documentation/android-sdk
[devsite-guide]: https://developers.google.com/maps/documentation/android-sdk/utility
[javadoc]: https://www.javadoc.io/doc/com.google.maps.android/android-maps-utils/latest/index.html
[android-maps-ktx]: https://github.com/googlemaps/android-maps-ktx
