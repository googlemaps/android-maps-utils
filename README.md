[![Build Status](https://travis-ci.org/googlemaps/android-maps-utils.svg?branch=master)](https://travis-ci.org/googlemaps/android-maps-utils)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.google.maps.android/android-maps-utils)
![GitHub contributors](https://img.shields.io/github/contributors/googlemaps/android-maps-utils?color=green)
[![Discord](https://img.shields.io/discord/676948200904589322)](https://discord.gg/hYsWbmk)
![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)

# Maps SDK for Android Utility Library

## Description

This open-source library contains utilities that are useful for a wide
range of applications using the [Google Maps Android API][android-site].

- **Marker clustering** — handles the display of a large number of points
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

You can view the generated [reference docs][javadoc] for a full list of classes and their methods.

## Requirements

* Android API level 15+
* Maps SDK via Google Play Services (this library is not yet compatible with the [Maps SDK v3.0 BETA] library)

## Installation

```groovy
dependencies {
    implementation 'com.google.maps.android:android-maps-utils:1.2.0'
}
```

## Migration Guide

Improvements made in version [1.0.0](https://github.com/googlemaps/android-maps-utils/releases/tag/1.0.0) of the library to support multiple layers on the map, caused breaking changes to versions prior to it. These changes also modify behaviors that are documented in the [Maps SDK for Android Maps documentation](https://developers.google.com/maps/documentation/android-sdk/intro) site. This section outlines all those changes and how you can migrate to use this library since version 1.0.0.

### Adding Click Events

Handling click events are handled on the layer specific object. Internally in each layer, the click handlers are passed to the marker, ground overlay, polyline, or polygon `Collection` object.

_New_
```java
// Clustering
ClusterManager<ClusterItem> clusterManager = // Initialize ClusterManager
clusterManager.setOnClusterItemClickListener(item -> {
    // Listen for clicks on a cluster item here
    return false;
});
clusterManager.setOnClusterClickListener(item -> {
    // Listen for clicks on a cluster here
    return false;
});

// GeoJson
GeoJsonLayer geoJsonLayer = // Initialize GeoJsonLayer
geoJsonLayer.setOnFeatureClickListener(feature -> {
    // Listen for clicks on GeoJson features here
});

// KML
KmlLayer kmlLayer = // Initialize KmlLayer
kmlLayer.setOnFeatureClickListener(feature -> {
    // Listen for clicks on KML features here
});
```

### Adding a Custom Info Window
_TODO_

### Adding a Marker Drag Listener
_TODO_

## Support

Encounter an issue while using this library?

If you find a bug or have a feature request, please [file an issue].
Or, if you'd like to contribute, send us a [pull request] and refer to our [code of conduct].

You can also reach us on our [Discord channel].

For more information, check out the detailed guide on the
[Google Developers site][devsite-guide].

[Maps SDK v3.0 BETA]: https://developers.google.com/maps/documentation/android-sdk/v3-client-migration
[file an issue]: https://github.com/googlemaps/android-maps-utils/issues/new/choose
[pull request]: https://github.com/googlemaps/android-maps-utils/compare
[code of conduct]: CODE_OF_CONDUCT.md
[Discord channel]: https://discord.gg/hYsWbmk
[android-site]: https://developer.android.com/training/maps/index.html
[devsite-guide]: https://developers.google.com/maps/documentation/android-api/utility/
[javadoc]: https://www.javadoc.io/doc/com.google.maps.android/android-maps-utils/latest/index.html
[android-maps-ktx]: https://github.com/googlemaps/android-maps-ktx
