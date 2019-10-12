# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/googlemaps/android-maps-utils/compare/0.6.1...master)

## [0.6.1](https://github.com/googlemaps/android-maps-utils/compare/0.6.0...0.6.1) - 2019-10-03
### Changed
- KML test files are no longer included in the library bundle [`#529`](https://github.com/googlemaps/android-maps-utils/pull/529)

## [0.6.0](https://github.com/googlemaps/android-maps-utils/compare/0.5.0...0.6.0) - 2019-10-02

### Merged

- publish to staged sonatype repository on tag [`#543`](https://github.com/googlemaps/android-maps-utils/pull/543)
- Update appcompat and core-ktx to 1.1.0 [`#542`](https://github.com/googlemaps/android-maps-utils/pull/542)
- autoformat code [`#540`](https://github.com/googlemaps/android-maps-utils/pull/540)
- Update Gradle plugin and wrapper [`#539`](https://github.com/googlemaps/android-maps-utils/pull/539)
- Fix build error, and upgrade dependencies. [`#535`](https://github.com/googlemaps/android-maps-utils/pull/535)
- Use HTTPS for demo KML Url [`#531`](https://github.com/googlemaps/android-maps-utils/pull/531)
- Upgrades tests to Junit4 [`#526`](https://github.com/googlemaps/android-maps-utils/pull/526)
- changes build to use maven-publish [`#525`](https://github.com/googlemaps/android-maps-utils/pull/525)
- update maps lib [`#518`](https://github.com/googlemaps/android-maps-utils/pull/518)
- Revert "Updates deploy to use maven-publish instead of maven" [`#524`](https://github.com/googlemaps/android-maps-utils/pull/524)
- Updates deploy to use maven-publish instead of maven [`#523`](https://github.com/googlemaps/android-maps-utils/pull/523)
- #488 Made Feature member id protected [`#490`](https://github.com/googlemaps/android-maps-utils/pull/490)
- Fix deprecated API usage warnings [`#481`](https://github.com/googlemaps/android-maps-utils/pull/481)
- Android P, Gradle 4.10, Android Studio 3.2.1 [`#495`](https://github.com/googlemaps/android-maps-utils/pull/495)
- Config for JitPack.io [`#479`](https://github.com/googlemaps/android-maps-utils/pull/479)
- Prevent throw "IllegalArgumentException: left == right" [`#478`](https://github.com/googlemaps/android-maps-utils/pull/478)
- Update PolyUtil.java [`#475`](https://github.com/googlemaps/android-maps-utils/pull/475)
- Fix #462: Typo in the resource name: amu_unknwown_folder [`#464`](https://github.com/googlemaps/android-maps-utils/pull/464)
- Fall back to linear interpolation when interpolating in short distances (#388). [`#458`](https://github.com/googlemaps/android-maps-utils/pull/458)
- Add the addLayerToMap method to Layer [`#455`](https://github.com/googlemaps/android-maps-utils/pull/455)
- Set polygon clickable option with style instead of forced true. [`#454`](https://github.com/googlemaps/android-maps-utils/pull/454)
- Update MarkerManager.java [`#446`](https://github.com/googlemaps/android-maps-utils/pull/446)
- Add documentation for onClusterClick [`#440`](https://github.com/googlemaps/android-maps-utils/pull/440)
- Small style fixes [`#410`](https://github.com/googlemaps/android-maps-utils/pull/410)
- Update KmlStyle.java [`#399`](https://github.com/googlemaps/android-maps-utils/pull/399)
- Update build tools and gradle. [`#390`](https://github.com/googlemaps/android-maps-utils/pull/390)
- Storing Collection objects in BiMultiMap [`#385`](https://github.com/googlemaps/android-maps-utils/pull/385)
- Don't run unit tests on Travis [`#370`](https://github.com/googlemaps/android-maps-utils/pull/370)
- Improve PolyUtil.isLocationOnPath to also tell where the location is on the polyline [`#361`](https://github.com/googlemaps/android-maps-utils/pull/361)
- Don't wrap Algorithm if already implements ScreenBasedAlgorithm [`#1`](https://github.com/googlemaps/android-maps-utils/pull/1)

### Fixed

- Fix #462: Typo in the resource name: amu_unknwown_folder (#464) [`#462`](https://github.com/googlemaps/android-maps-utils/issues/462)

### Commits

- Revert "Updates deploy to use maven-publish instead of maven (#523)" [`4c76e34`](https://github.com/googlemaps/android-maps-utils/commit/4c76e3405fac6572fd267a89398758cdeed49f0b)
- Fixes issue where demo was not building [`f359ec0`](https://github.com/googlemaps/android-maps-utils/commit/f359ec088a5c01ea17d732e835e4dbc3b8f52a2b)
- Handle altitudes and parse Track and MultiTrack objects [`4ee2302`](https://github.com/googlemaps/android-maps-utils/commit/4ee230208200fe68acf9d7b155811d84d472abbb)

## [0.5.0](https://github.com/googlemaps/android-maps-utils/compare/0.4.4...0.5.0) - 2017-02-01

### Merged

- GeoJSON/KML Integration [`#351`](https://github.com/googlemaps/android-maps-utils/pull/351)
- Update PolyUtil.java [`#347`](https://github.com/googlemaps/android-maps-utils/pull/347)
- Disable animations [`#334`](https://github.com/googlemaps/android-maps-utils/pull/334)
- made KMLStyle class public [`#335`](https://github.com/googlemaps/android-maps-utils/pull/335)
- Add new containsLocation() that takes latitude and longitude instead of LatLng [`#331`](https://github.com/googlemaps/android-maps-utils/pull/331)
- Multi polygon click listener issue [`#332`](https://github.com/googlemaps/android-maps-utils/pull/332)
- Infwindow [`#329`](https://github.com/googlemaps/android-maps-utils/pull/329)
- Added else if statement in setMarkerInfoWindow in KmlRenderer for null exception bug fix. [`#330`](https://github.com/googlemaps/android-maps-utils/pull/330)
- Reuse cluster markers [`#321`](https://github.com/googlemaps/android-maps-utils/pull/321)
- Fix #308 - Add null check for removing item from uninitialized QuadTree [`#314`](https://github.com/googlemaps/android-maps-utils/pull/314)
- Bump versions [`#318`](https://github.com/googlemaps/android-maps-utils/pull/318)

### Fixed

- Fix #308 - Add null check for removing item from uninitialized QuadTree (#314) [`#308`](https://github.com/googlemaps/android-maps-utils/issues/308)

### Commits

- Configurable max distance between clustered items [`735af48`](https://github.com/googlemaps/android-maps-utils/commit/735af4888981b8101f52007ddfb2a46b678ee281)
- Add some useful methods for MarkerManager. [`8fd69a6`](https://github.com/googlemaps/android-maps-utils/commit/8fd69a6ba17999f884750e91bad220d726244b9a)
- Add go button in Heatmaps-Places demo. [`862a44c`](https://github.com/googlemaps/android-maps-utils/commit/862a44cb0a5c8dee8de7f42ae8822ba2d12189a4)

## [0.4.4](https://github.com/googlemaps/android-maps-utils/compare/0.4.3...0.4.4) - 2016-08-29

### Merged

- Add Github issue template [`#302`](https://github.com/googlemaps/android-maps-utils/pull/302)
- Updated API key name to match documentation https://developers.google.com/maps/documentation/android-api/signup#add_the_api_key_to_your_application [`#300`](https://github.com/googlemaps/android-maps-utils/pull/300)
- Fix #284 - Tweak Travis script to fix Travis builds [`#299`](https://github.com/googlemaps/android-maps-utils/pull/299)
- Add self to contributors list [`#301`](https://github.com/googlemaps/android-maps-utils/pull/301)
- New camera listeners [`#295`](https://github.com/googlemaps/android-maps-utils/pull/295)
- Kml draw order [`#293`](https://github.com/googlemaps/android-maps-utils/pull/293)
- Handle KML colors with missing leading zero [`#291`](https://github.com/googlemaps/android-maps-utils/pull/291)
- Update GeoJson demo to also include a local resource example. [`#290`](https://github.com/googlemaps/android-maps-utils/pull/290)
- Null GeoJson feature properties [`#289`](https://github.com/googlemaps/android-maps-utils/pull/289)
- Add support for click listeners on GeoJSON layers. [`#286`](https://github.com/googlemaps/android-maps-utils/pull/286)
- KML renderer is not an activity [`#268`](https://github.com/googlemaps/android-maps-utils/pull/268)
- Misc cleanup [`#285`](https://github.com/googlemaps/android-maps-utils/pull/285)
- Gradle version bump + updated KML URL for demo. [`#283`](https://github.com/googlemaps/android-maps-utils/pull/283)
- Adds prefix to resources and fixes build issues. [`#264`](https://github.com/googlemaps/android-maps-utils/pull/264)
- Replace deprecated getMap with getMapAsync in demo. [`#255`](https://github.com/googlemaps/android-maps-utils/pull/255)

### Fixed

- Fix #284 - Tweak Travis script to fix Travis builds (#299) [`#284`](https://github.com/googlemaps/android-maps-utils/issues/284)
- Add support for drawOrder element in KML. Closes #240. [`#240`](https://github.com/googlemaps/android-maps-utils/issues/240)
- Handle KML colors with missing leading zero, exported from Google Maps. Closes #232. [`#232`](https://github.com/googlemaps/android-maps-utils/issues/232)
- Add getters for ClusterManger algorithm/renderer. Closes #149. [`#149`](https://github.com/googlemaps/android-maps-utils/issues/149)
- Add demo gif to README. Closes #158. [`#158`](https://github.com/googlemaps/android-maps-utils/issues/158)

### Commits

- Refactored NonHierarchicalDistanceBasedAlgorithm for inheritence. [`8694da3`](https://github.com/googlemaps/android-maps-utils/commit/8694da38220d6394e4d9216b2a359d536afe9c40)
- Make BiMultiMap package private. [`dcf33d6`](https://github.com/googlemaps/android-maps-utils/commit/dcf33d688079ef8c15a4ad0c89824e16611e14db)
- Fix BiMultiMap.remove() and implement missing methods (clear, clone). [`61ab2f0`](https://github.com/googlemaps/android-maps-utils/commit/61ab2f045c4fdb1f48776dd60926746782ee862b)

## [0.4.3](https://github.com/googlemaps/android-maps-utils/compare/0.4.2...0.4.3) - 2016-03-03

### Merged

- Added sourceJar task to library [`#250`](https://github.com/googlemaps/android-maps-utils/pull/250)
- Increasing ADB timeout to stabalise builds [`#251`](https://github.com/googlemaps/android-maps-utils/pull/251)

### Commits

- snapshot version [`993516b`](https://github.com/googlemaps/android-maps-utils/commit/993516b89be432fae48190edd4bb08d13da38d01)

## [0.4.2](https://github.com/googlemaps/android-maps-utils/compare/0.4.0...0.4.2) - 2016-02-24

### Merged

- Added the setClickable method [`#247`](https://github.com/googlemaps/android-maps-utils/pull/247)
- Allow numeric boolean parsing in kml styles [`#228`](https://github.com/googlemaps/android-maps-utils/pull/228)
- Equals for cluster subclasses [`#245`](https://github.com/googlemaps/android-maps-utils/pull/245)
- makeIcon takes CharSequence not String [`#244`](https://github.com/googlemaps/android-maps-utils/pull/244)
- Prevent crash on null Map Projection [`#229`](https://github.com/googlemaps/android-maps-utils/pull/229)
- Fix #207 - Add Travis CI support [`#209`](https://github.com/googlemaps/android-maps-utils/pull/209)
- Fix #204 - Don't change reference for last point in PolyUtil.simplify() [`#205`](https://github.com/googlemaps/android-maps-utils/pull/205)
- Fix #187 - Update QuadTree tests to be inclusive of points on max X/Y… [`#206`](https://github.com/googlemaps/android-maps-utils/pull/206)
- Add Douglas-Peucker line simplification algorithm as PolyUtil.simplify() [`#201`](https://github.com/googlemaps/android-maps-utils/pull/201)
- Make getColor protected in DefaultClusterRenderer [`#199`](https://github.com/googlemaps/android-maps-utils/pull/199)
- Include only maps component of Google Play Services library [`#202`](https://github.com/googlemaps/android-maps-utils/pull/202)
- Update latest gradle versions and android dependencies. [`#200`](https://github.com/googlemaps/android-maps-utils/pull/200)

### Fixed

- Merge pull request #209 from barbeau/travis-rebase [`#207`](https://github.com/googlemaps/android-maps-utils/issues/207)
- Fix #207 - Add Travis CI support [`#207`](https://github.com/googlemaps/android-maps-utils/issues/207)
- Merge pull request #205 from barbeau/simplifyInputChange [`#204`](https://github.com/googlemaps/android-maps-utils/issues/204)
- Merge pull request #206 from barbeau/187-PointQuadTreeTestFail [`#187`](https://github.com/googlemaps/android-maps-utils/issues/187)
- Fix #187 - Update QuadTree tests to be inclusive of points on max X/Y bounds [`#187`](https://github.com/googlemaps/android-maps-utils/issues/187)
- Fix #204 - Don't change reference for last point input to PolyUtil.simplify() [`#204`](https://github.com/googlemaps/android-maps-utils/issues/204)

### Commits

- Add Douglas-Peucker poly simplification algorithm as PolyUtil.simplify() [`d724493`](https://github.com/googlemaps/android-maps-utils/commit/d7244938e7c7dc31dd398a042ab5d44acd3217e3)
- Fixed screen size computation in VisibleNonHierarchicalDistanceBasedAlgorithm. Sow screen size is based on display, but not on map left and right corners coordinates. [`0ce7080`](https://github.com/googlemaps/android-maps-utils/commit/0ce708011587923d4217cfa1a186e65836b43c9a)
- Updated according to your suggestions [`064b1d6`](https://github.com/googlemaps/android-maps-utils/commit/064b1d66fb7257c3d72b3e3bb30ed17b54e29202)

## [0.4.0](https://github.com/googlemaps/android-maps-utils/compare/0.3.4...0.4.0) - 2015-07-29

### Merged

- Add section for Gradle users and Maven Central badge. [`#130`](https://github.com/googlemaps/android-maps-utils/pull/130)
- Update project setup [`#173`](https://github.com/googlemaps/android-maps-utils/pull/173)
- Remove sRGB color profile from PNGs [`#177`](https://github.com/googlemaps/android-maps-utils/pull/177)
- Convert line endings from CRLF to LF. [`#172`](https://github.com/googlemaps/android-maps-utils/pull/172)
- Track gradle versions - 1.0 now required [`#140`](https://github.com/googlemaps/android-maps-utils/pull/140)

### Commits

- KML overlay (PR #155) [`d606fcd`](https://github.com/googlemaps/android-maps-utils/commit/d606fcde40467abb5fae2ba78b8562a2cd1c517b)
- GeoJSON overlay (PR #156) [`46a4e20`](https://github.com/googlemaps/android-maps-utils/commit/46a4e20b034880c659689ff106931827ccaf2510)
- Add missing license headers [`b72f44e`](https://github.com/googlemaps/android-maps-utils/commit/b72f44e97700147a8178ad8b9187af155368da87)

## [0.3.4](https://github.com/googlemaps/android-maps-utils/compare/0.3.2...0.3.4) - 2014-12-17

### Merged

- Replaced play-services dependency with play-services-maps [`#137`](https://github.com/googlemaps/android-maps-utils/pull/137)
- Fix faulty setup of views in IconGenerator [`#129`](https://github.com/googlemaps/android-maps-utils/pull/129)

### Commits

- Update build tools version [`85258de`](https://github.com/googlemaps/android-maps-utils/commit/85258dec6c217c5189b453478f691375f2a14db1)
- Tag 0.3.4 [`7e97ab6`](https://github.com/googlemaps/android-maps-utils/commit/7e97ab6405e17499a53b3e3a5b07f03381d62781)
- Prepare for next version [`ee924f7`](https://github.com/googlemaps/android-maps-utils/commit/ee924f7f074a9a24a87c8427f04778e21f95f5e6)

## [0.3.2](https://github.com/googlemaps/android-maps-utils/compare/0.3.1...0.3.2) - 2014-10-22

### Merged

- Add "IconGenerator.setColor", add xxhdpi resources [`#124`](https://github.com/googlemaps/android-maps-utils/pull/124)
- Update build scripts [`#123`](https://github.com/googlemaps/android-maps-utils/pull/123)
- Add lock check missing from waitUntilFree method. [`#122`](https://github.com/googlemaps/android-maps-utils/pull/122)
- Removed duplicate methods [`#120`](https://github.com/googlemaps/android-maps-utils/pull/120)
- Fix #115 by updating gradle build info and manifest [`#117`](https://github.com/googlemaps/android-maps-utils/pull/117)
- ClusterManager, used THREAD_POOL_EXECUTOR [`#119`](https://github.com/googlemaps/android-maps-utils/pull/119)
- ConcurrentHashMap backed sets to avoid concurrent modification exceptions [`#109`](https://github.com/googlemaps/android-maps-utils/pull/109)
- removed import com.google.android.gms.maps.model.MarkerOption as its no ... [`#108`](https://github.com/googlemaps/android-maps-utils/pull/108)
- Make methods to get ClusterItem and Markers public [`#95`](https://github.com/googlemaps/android-maps-utils/pull/95)
- Finish pull request for issue #50 (mapping markers to clusters, cluster items, and vice-versa) [`#67`](https://github.com/googlemaps/android-maps-utils/pull/67)
- Fixes Bounds.contains() for issue #71 [`#76`](https://github.com/googlemaps/android-maps-utils/pull/76)

### Fixed

- Merge pull request #117 from rfay/20141008_fix_gradle_build [`#115`](https://github.com/googlemaps/android-maps-utils/issues/115)
- Fix #115 by updating gradle build info and manifest [`#115`](https://github.com/googlemaps/android-maps-utils/issues/115)

### Commits

- ConcurrentHashMap backed sets to avoid concurrent modification exceptions. [`512985e`](https://github.com/googlemaps/android-maps-utils/commit/512985ea17c27d026a3fa597c4b71117967fa9f7)
- ConcurrentHashMap style changes [`d176ce3`](https://github.com/googlemaps/android-maps-utils/commit/d176ce381ac144593343d2981ab4ac53b016f843)
- Tag 0.3.2 [`90d422f`](https://github.com/googlemaps/android-maps-utils/commit/90d422f59138d5c0f7071cbaa3840fa38dbf09c6)

## [0.3.1](https://github.com/googlemaps/android-maps-utils/compare/0.2.1...0.3.1) - 2014-04-28

### Merged

- Fixed warnings [`#58`](https://github.com/googlemaps/android-maps-utils/pull/58)
- Bug fix [`#21`](https://github.com/googlemaps/android-maps-utils/pull/21)
- Fix compiler and lint warnings [`#53`](https://github.com/googlemaps/android-maps-utils/pull/53)
- Adding attributions for data set [`#20`](https://github.com/googlemaps/android-maps-utils/pull/20)
- Adding licensing and cleaning up PointQuadTreeTest [`#19`](https://github.com/googlemaps/android-maps-utils/pull/19)
- removed Log import [`#18`](https://github.com/googlemaps/android-maps-utils/pull/18)
- Fixed comments [`#17`](https://github.com/googlemaps/android-maps-utils/pull/17)
- Exclude files generated by ADT from git [`#54`](https://github.com/googlemaps/android-maps-utils/pull/54)
- Fixing things that admo pointed out [`#16`](https://github.com/googlemaps/android-maps-utils/pull/16)
- Code Review Fixes [`#15`](https://github.com/googlemaps/android-maps-utils/pull/15)
- Comment fixes [`#14`](https://github.com/googlemaps/android-maps-utils/pull/14)
- Adding places demo :) [`#13`](https://github.com/googlemaps/android-maps-utils/pull/13)
- Removed another data set [`#12`](https://github.com/googlemaps/android-maps-utils/pull/12)
- Changed datasets, Tidying up [`#11`](https://github.com/googlemaps/android-maps-utils/pull/11)
- Removed interpolator and changed colours [`#10`](https://github.com/googlemaps/android-maps-utils/pull/10)
- Gradient Class :) [`#9`](https://github.com/googlemaps/android-maps-utils/pull/9)
- Fix cluster click listeners [`#47`](https://github.com/googlemaps/android-maps-utils/pull/47)
- getMaxValue optimisations [`#8`](https://github.com/googlemaps/android-maps-utils/pull/8)
- Optimised + timing [`#5`](https://github.com/googlemaps/android-maps-utils/pull/5)
- Refactoring, colours, tests [`#7`](https://github.com/googlemaps/android-maps-utils/pull/7)
- Move to TileProvider [`#4`](https://github.com/googlemaps/android-maps-utils/pull/4)
- Add info window click listeners [`#44`](https://github.com/googlemaps/android-maps-utils/pull/44)
- More refactoring [`#3`](https://github.com/googlemaps/android-maps-utils/pull/3)
- Heatmaps uses old quadtree [`#2`](https://github.com/googlemaps/android-maps-utils/pull/2)
- Switched back to old quadtree [`#6`](https://github.com/googlemaps/android-maps-utils/pull/6)
- More tidying up [`#5`](https://github.com/googlemaps/android-maps-utils/pull/5)
- Removed get*Quad methods and refactored more thing [`#4`](https://github.com/googlemaps/android-maps-utils/pull/4)
- All the heatmaps stuff [`#1`](https://github.com/googlemaps/android-maps-utils/pull/1)
- Datasets [`#3`](https://github.com/googlemaps/android-maps-utils/pull/3)
- Heatmaps [`#2`](https://github.com/googlemaps/android-maps-utils/pull/2)

### Fixed

- AntonioGonzalez' changes to fix #49 [`#49`](https://github.com/googlemaps/android-maps-utils/issues/49)

### Commits

- Changed datasets [`bef3986`](https://github.com/googlemaps/android-maps-utils/commit/bef3986a406902b00d49a154843ecef94536e4ef)
- Util functions moved into TileProvider [`32b6b0d`](https://github.com/googlemaps/android-maps-utils/commit/32b6b0d5e3707d9edd4fb951f22b0b983576f775)
- Removed Helper. User now creates a HeatmapTileProvider.Builder and adds it to the map (.addTileOverlay) [`891e7a0`](https://github.com/googlemaps/android-maps-utils/commit/891e7a0fc1e4f0e73d69cf8d8e0e78e403276730)

## 0.2.1 - 2014-01-09

### Merged

- Update to quadTrees [`#1`](https://github.com/googlemaps/android-maps-utils/pull/1)
- Area: new implementation avoiding isCCW, and taking radius. [`#21`](https://github.com/googlemaps/android-maps-utils/pull/21)
- General code clean up [`#24`](https://github.com/googlemaps/android-maps-utils/pull/24)
- Fix dark text appearance in TextIconGenerator [`#25`](https://github.com/googlemaps/android-maps-utils/pull/25)
- Fix PointQuadTree test. [`#22`](https://github.com/googlemaps/android-maps-utils/pull/22)
- Add PolyUtil isLocationOnEdge, isLocationOnPath [`#19`](https://github.com/googlemaps/android-maps-utils/pull/19)
- Update Earth radius - use the mean radius of the earth instead of radius at equator. [`#20`](https://github.com/googlemaps/android-maps-utils/pull/20)
- Rename BubbleIconFactory to TextIconGenerator, remove use of enums. [`#16`](https://github.com/googlemaps/android-maps-utils/pull/16)
- Add IntelliJ project files and Gradle build directory to gitignore. [`#15`](https://github.com/googlemaps/android-maps-utils/pull/15)
- Refactoring of demos. Change demo minSdkVersion to 8. [`#14`](https://github.com/googlemaps/android-maps-utils/pull/14)
- Call startDemo() only once. [`#13`](https://github.com/googlemaps/android-maps-utils/pull/13)
- Add PolyUtil.containsLocation(). [`#11`](https://github.com/googlemaps/android-maps-utils/pull/11)
- Add a Bitdeli Badge to README [`#9`](https://github.com/googlemaps/android-maps-utils/pull/9)

### Commits

- Remove docs [`a5235a1`](https://github.com/googlemaps/android-maps-utils/commit/a5235a19b1575bff7d5ca28680d9c3498cf15dd6)
- Add Javadocs [`7353a5f`](https://github.com/googlemaps/android-maps-utils/commit/7353a5fb8b3147a5b7edf141bb736f55b40bc9ea)
- Update docs [`2cd1a0f`](https://github.com/googlemaps/android-maps-utils/commit/2cd1a0f412531a6f1a44b4ea13840720d17b7bc1)
