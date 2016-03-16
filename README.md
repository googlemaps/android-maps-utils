# Google Maps Android API utility library

## Introduction

This is customized repository for my use.

The original repository is [here](https://github.com/googlemaps/android-maps-utils).

## What's changed

I solved [this problem](https://github.com/googlemaps/android-maps-utils/issues/257) using [clipper-java](https://github.com/lightbringer/clipper-java) removed lambda.

## Usage

Compile and copy **\*.aar** to your project.

```
git clone https://github.com/SeijiIto/android-maps-utils.git
cd android-maps-utils
./gradlew assembleDebug assembleRelease
cp library/build/outputs/aar/android-maps-utils-{debug,release}.aar YOUR_PROJECT_ROOT/app/libs/
```

And add settings to **app/build.gradle** in your project.

```
repositories {
    flatDir { dirs 'libs' }
}

dependencies {
    releaseCompile(name:'android-maps-utils-release', ext:'aar')
    debugCompile(name:'android-maps-utils-debug', ext:'aar')
}
```
