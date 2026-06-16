# Changelog

## [4.5.0](https://github.com/googlemaps/android-maps-utils/compare/v4.4.1...v4.5.0) (2026-06-16)


### Features

* add manual RC release workflow ([#1695](https://github.com/googlemaps/android-maps-utils/issues/1695)) ([688fc83](https://github.com/googlemaps/android-maps-utils/commit/688fc833ba55235450096546b22d3ac0db73d59b))


### Bug Fixes

* change model name from gemini-2.5-flash to gemini-3.5-flash ([#1696](https://github.com/googlemaps/android-maps-utils/issues/1696)) ([838a2bf](https://github.com/googlemaps/android-maps-utils/commit/838a2bf0621768c7dbdd6daa30db5522d90102ec))
* enable filterTouchesWhenObscured on WebView ([#1703](https://github.com/googlemaps/android-maps-utils/issues/1703)) ([0674d75](https://github.com/googlemaps/android-maps-utils/commit/0674d7504d048e668357e2e9dbd2d923fbea20f5))

## [4.4.1](https://github.com/googlemaps/android-maps-utils/compare/v4.4.0...v4.4.1) (2026-05-26)


### Bug Fixes

* address lint, compiler, and Error Prone warnings ([#1692](https://github.com/googlemaps/android-maps-utils/issues/1692)) ([ce809c7](https://github.com/googlemaps/android-maps-utils/commit/ce809c741ce37611a25efce2b1b95f95d2e108fe))

## [4.4.0](https://github.com/googlemaps/android-maps-utils/compare/v4.3.0...v4.4.0) (2026-05-20)


### Features

* add navigation flavor to demo for SDK 7.x reproduction ([#1680](https://github.com/googlemaps/android-maps-utils/issues/1680)) ([12ac29a](https://github.com/googlemaps/android-maps-utils/commit/12ac29a3db8c96bd34536581f5f5cf665a9c4f76))

## [4.3.0](https://github.com/googlemaps/android-maps-utils/compare/v4.2.0...v4.3.0) (2026-04-24)


### Features

* add constructor parameter for custom Executor in cluster renderers ([#1682](https://github.com/googlemaps/android-maps-utils/issues/1682)) ([b803dc0](https://github.com/googlemaps/android-maps-utils/commit/b803dc0602417806d1b029498c60a7111ffed723))

## [4.2.0](https://github.com/googlemaps/android-maps-utils/compare/v4.1.1...v4.2.0) (2026-04-22)


### Features

* KML URL Sanitizer API ([#1678](https://github.com/googlemaps/android-maps-utils/issues/1678)) ([43855e9](https://github.com/googlemaps/android-maps-utils/commit/43855e9128157959a00fe8797d19b8340b18534e))


### Bug Fixes

* disable XML external entities in KML parser ([#1673](https://github.com/googlemaps/android-maps-utils/issues/1673)) ([4516be3](https://github.com/googlemaps/android-maps-utils/commit/4516be350af570b466adf71cf2521ec4ac1a3f58))
* KMZ zip bomb mitigation by adding entry and size limits ([#1677](https://github.com/googlemaps/android-maps-utils/issues/1677)) ([bae3455](https://github.com/googlemaps/android-maps-utils/commit/bae3455ebe5b1fde99a6bbe37348f5bfef62a429))
* prevent NPE in LatLngBounds.contains by adding null checks for positions ([#1675](https://github.com/googlemaps/android-maps-utils/issues/1675)) ([6b5a2d3](https://github.com/googlemaps/android-maps-utils/commit/6b5a2d32ebb8b4b43db5fa0152e2ca4f7cea73e0))
* restrict KML image downloads to http/https schemes ([#1674](https://github.com/googlemaps/android-maps-utils/issues/1674)) ([898ae8f](https://github.com/googlemaps/android-maps-utils/commit/898ae8f2c754988e55ced509c6cd2b8bd09414ec))

## [4.1.1](https://github.com/googlemaps/android-maps-utils/compare/v4.1.0...v4.1.1) (2026-03-11)


### Bug Fixes

* trigger release for PR [#1661](https://github.com/googlemaps/android-maps-utils/issues/1661) ([#1662](https://github.com/googlemaps/android-maps-utils/issues/1662)) ([8a96267](https://github.com/googlemaps/android-maps-utils/commit/8a9626717455d386f820ab3115b1b20080da8289))

## [4.1.0](https://github.com/googlemaps/android-maps-utils/compare/v4.0.0...v4.1.0) (2026-02-20)


### Features

* Transit Layer Demo & Quality Improvements ([#1653](https://github.com/googlemaps/android-maps-utils/issues/1653)) ([cb074f9](https://github.com/googlemaps/android-maps-utils/commit/cb074f93d3281c3e0b7f0a89b88644b8044db2a4))

## [4.0.0](https://github.com/googlemaps/android-maps-utils/compare/v3.20.1...v4.0.0) (2026-01-23)


### ⚠ BREAKING CHANGES

* update to maps 20.0.0 ([#1644](https://github.com/googlemaps/android-maps-utils/issues/1644))

### Features

* update to maps 20.0.0 ([#1644](https://github.com/googlemaps/android-maps-utils/issues/1644)) ([60adcde](https://github.com/googlemaps/android-maps-utils/commit/60adcde90c9132d7bbf11618690072e4fa593301))
