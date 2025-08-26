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
* [Sign up with Google Maps Platform]
* A Google Maps Platform [project] with the **Maps SDK for Android** enabled
- An [API key] associated with the project above ... follow the [API key instructions] if you're new to the process

## Installation

```groovy
dependencies {
    // Utilities for Maps SDK for Android (requires Google Play Services)
    // You do not need to add a separate dependency for the Maps SDK for Android
    // since this library builds in the compatible version of the Maps SDK.
    implementation 'com.google.maps.android:android-maps-utils:3.16.0'

    // Optionally add the Kotlin Extensions (KTX) for full Kotlin language support
    // See latest version at https://github.com/googlemaps/android-maps-ktx
    // implementation 'com.google.maps.android:maps-utils-ktx:<latest-version>'
}
```

## Sample App

<img src="https://developers.google.com/maps/documentation/android-sdk/images/utility-markercluster.png" width="150" align=right>

This repository includes a [sample app](demo) that illustrates the use of this library.

To run the demo app, ensure you've met the requirements above then:
1. Clone the repository
1. Add a file `local.properties` in the root project (this file should *NOT* be under version control to protect your API key)
1. Add a single line to `local.properties` that looks like `MAPS_API_KEY=YOUR_API_KEY`, where `YOUR_API_KEY` is the API key you obtained earlier
1. Build and run the `debug` variant for the Maps SDK for Android version

## Documentation

See the [documentation] for a full list of classes and their methods.

Full guides for using the utilities are published in
[Google Maps Platform documentation](https://developers.google.com/maps/documentation/android-sdk/utility).

## Usage

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

## Contributing

Contributions are welcome and encouraged! If you'd like to contribute, send us a [pull request] and refer to our [code of conduct] and [contributing guide].

## Terms of Service

This library uses Google Maps Platform services. Use of Google Maps Platform services through this library is subject to the Google Maps Platform [Terms of Service].

If your billing address is in the European Economic Area, effective on 8 July 2025, the [Google Maps Platform EEA Terms of Service](https://cloud.google.com/terms/maps-platform/eea) will apply to your use of the Services. Functionality varies by region. [Learn more](https://developers.google.com/maps/comms/eea/faq).

This library is not a Google Maps Platform Core Service. Therefore, the Google Maps Platform Terms of Service (e.g. Technical Support Services, Service Level Agreements, and Deprecation Policy) do not apply to the code in this library.

## Support

This library is offered via an open source [license]. It is not governed by the Google Maps Platform Support [Technical Support Services Guidelines, the SLA, or the [Deprecation Policy]. However, any Google Maps Platform services used by the library remain subject to the Google Maps Platform Terms of Service.

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

