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

## Requirements

* Android API level 15+
* Maps SDK via Google Play Services (this library is not yet compatible with the [Maps SDK v3.0 BETA] library)

## Installation

```groovy
dependencies {
    implementation 'com.google.maps.android:android-maps-utils:1.0.2'
}
```

## Support

Encounter an issue while using this library?

If you find a bug or have a feature request, please [file an issue].
Or, if you'd like to contribute, send us a [pull request] and refer to our [code of conduct].

You can also reach us on our [Discord channel].

For more information, check out the detailed guide on the
[Google Developers site][devsite-guide]. You can also view the generated
[reference docs][javadoc] for a full list of classes and their methods.

[Maps SDK v3.0 BETA]: https://developers.google.com/maps/documentation/android-sdk/v3-client-migration
[file an issue]: https://github.com/googlemaps/android-maps-utils/issues/new/choose
[pull request]: https://github.com/googlemaps/android-maps-utils/compare
[code of conduct]: CODE_OF_CONDUCT.md
[Discord channel]: https://discord.gg/hYsWbmk
[android-site]: https://developer.android.com/training/maps/index.html
[devsite-guide]: https://developers.google.com/maps/documentation/android-api/utility/
[javadoc]: http://googlemaps.github.io/android-maps-utils/javadoc/
[android-maps-ktx]: https://github.com/googlemaps/android-maps-ktx
