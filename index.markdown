---
layout: default
---

## Introduction

This open-source library contains classes that are useful for a wide range of applications using the [Google Maps Android API](http://developer.android.com/google/play-services/maps.html).

The library is under heavy development, but ready for use. Check the [issue tracker][issues] to see what's happening.

[github]: https://github.com/googlemaps/android-maps-utils
[issues]: https://github.com/googlemaps/android-maps-utils/issues

<p id="overview"></p>

## Features/Overview

<img src="bubblemarker.png" style="float: right">

  * **Marker clustering** &mdash; handles the display of a large number of points

  * **Heat maps** &mdash; display a large number of points as a heat map

  * **IconGenerator** &mdash; display text on your Markers (see screenshot to the right)

  * **Poly decoding and encoding** &mdash; compact encoding for paths, [interoperability](https://developers.google.com/maps/documentation/utilities/polylinealgorithm) with Maps API web services

  * **Spherical geometry** &mdash; for example: computeDistance, computeHeading, computeArea

  * **KML** &mdash; displays KML data (Caution: Beta!)

  * **GeoJSON** &mdash; displays and styles GeoJSON data

<br style="clear:both">
An introductory video:

<iframe width="560" height="315" src="https://www.youtube.com/embed/nb2X9IjjZpM" frameborder="0" allowfullscreen></iframe>

<p id="start"></p>

## Getting Started

The current version of the library is `0.4`.

### Android Studio/Gradle

Add the following dependency to your Gradle build file:

    dependencies {
        compile 'com.google.maps.android:android-maps-utils:0.4+'
    }

### Maven

Add the following to your `pom.xml`:

    <dependency>
        <groupId>com.google.maps.android</groupId>
        <artifactId>android-maps-utils-apklib</artifactId>
        <version>0.4+</version>
        <type>apklib</type>
    </dependency>

_Note: you must have Google Play services included in your project already.
See [JakeWharton/gms-mvn-install](https://github.com/JakeWharton/gms-mvn-install) for one way to do this._

### Eclipse/ADT

The library is distributed as a Android library project.
See the [guide on Android developers](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject) for instructions on setting up a library project.

Check out the repository from [GitHub][github] and reference the `library` subdirectory.

_(zip download is coming soon)_

<!--a class="button" href="android-maps-utils-0.2.1.zip">Download libproject zip</a-->

<p id="feedback"></p>

## Feedback

Feedback and feature requests can be filed on the GitHub [issue tracker][issues].

<p id="source"></p>

## Source code

The project is hosted on [GitHub][github].

### Building the project

The project uses the Gradle build system. Use of the Gradle wrapper (`gradlew`) is preferred, as it keeps the build more contained ("hermetic", if you enjoy that kind of thing).</p>

Ensure your `ANDROID_HOME` environment variable is set correctly. This may be something like `"/Applications/Android Studio.app/sdk/"` on Mac OS X.

You will also need to download the "Google Repository" in the SDK Manager. (You should be using it in your project already!)

    # Build everything
    $ ./gradlew assemble

    # Run tests
    $ ./gradlew instrumentTest

    # Install demo app on connected device
    # Don't forget to put your own API key in AndroidManifest.xml!
    $ ./gradlew installDebug


<p id="contribute"></p>

## Contributions

Contributions are welcomed. You can submit a pull request via
[GitHub][github].

For your first contribution, you will need to fill out one of the contributor license agreements:

  * If you are the copyright holder, you will need to agree to the <a href="https://developers.google.com/open-source/cla/individual?csw=1">individual contributor license agreement</a>, which can be completed online.
  * If your organization is the copyright holder, the organization will need to agree to the <a href="http://code.google.com/legal/corporate-cla-v1.0.html">corporate contributor license agreement</a>. (If the copyright holder for your code has already completed the agreement in connection with another Google open source project, it does not need to be completed again.)

<p id="license"></p>

## License

    Copyright 2013 Google Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

<p id="illustration">
<a href="http://mkstvns.com">
<img src="androidmaps-mkstvns.png" title="Illustration by Mike Stevens (mkstvns.com) - all rights reserved">
</a>
</p>
