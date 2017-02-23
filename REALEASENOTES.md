# Google Maps Android API Utility Library Release Notes

### Version 0.5

#### New Features:

  * Click handling was previously only available for GeoJSON, but is now available for KML layers as well
    - Also works for multipolygons, multilinestring and multipoints
  * Info windows can be added to clustered markers


#### Modifications from previous version:
 
 *  OnFeatureClickListener changed from accepting a GeoJsonFeature to a Feature, which is supported for KML as well.
 
    Previous version:
    ```java
	public interface GeoJsonOnFeatureClickListener {
        void onFeatureClick(GeoJsonFeature feature);
    }
	```
    New version:
    
    ```java
	public interface GeoJsonOnFeatureClickListener {
        void onFeatureClick(Feature feature);
    }
	```
 * GeoJsonGeometry and KmlGeometry interfaces replaced by common Geometry interface
 * ClusterItem interface has two new methods:
 	`getTitle()` and
    `getSnippet()`


#### Issues resolved:

 * [NullPointerException with markers that have snippets but no title (Issue #327)](https://github.com/googlemaps/android-maps-utils/issues/327)
   * [Issue #218](https://github.com/googlemaps/android-maps-utils/issues/218)
   * [Issue #317](https://github.com/googlemaps/android-maps-utils/issues/317)
   
 * [Multipolygon not supported by OnFeatureClickListener (Issue #320)](https://github.com/googlemaps/android-maps-utils/issues/320)
 * [KmlStyle class not public (Issue #311)](https://github.com/googlemaps/android-maps-utils/issues/311)
 * [Info Windows on clustered markers (Issue #188)](https://github.com/googlemaps/android-maps-utils/issues/188#issuecomment-262714692)
 * [Disable animations for clustered markers (Issue #306)](https://github.com/googlemaps/android-maps-utils/issues/306)



