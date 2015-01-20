package com.google.maps.android.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Renders GeoJsonFeature objects onto the GoogleMap as Marker, Polyline and Polygon objects. Also
 * removes GeoJsonFeature objects and redraws features when updated.
 */
/* package */ class GeoJsonRenderer implements Observer {

    private final static int POLYGON_OUTER_COORDINATE_INDEX = 0;

    private final static int POLYGON_INNER_COORDINATE_INDEX = 1;

    private final static Object FEATURE_NOT_ON_MAP_VALUE = null;

    /**
     * Value is a Marker, Polyline, Polygon or an array of these that have been created from the
     * corresponding key
     */
    private final HashMap<GeoJsonFeature, Object> mFeatures;

    private GoogleMap mMap;

    /**
     * Creates a new GeoJsonRender object
     *
     * @param map map to place GeoJsonFeature objects on
     */
    /* package */ GeoJsonRenderer(GoogleMap map) {
        mFeatures = new HashMap<GeoJsonFeature, Object>();
        mMap = map;
    }

    /**
     * Gets a Marker, Polyline, Polygon or an array of these and removes from the map
     *
     * @param mapObject map object or array of map objects to remove from the map
     */
    private static void removeFromMap(Object mapObject) {
        if (mapObject instanceof Marker) {
            ((Marker) mapObject).remove();
        } else if (mapObject instanceof Polyline) {
            ((Polyline) mapObject).remove();
        } else if (mapObject instanceof Polygon) {
            ((Polygon) mapObject).remove();
        } else if (mapObject instanceof ArrayList) {
            for (Object mapObjectElement : (ArrayList) mapObject) {
                removeFromMap(mapObjectElement);
            }
        }
    }

    /**
     * Gets the GoogleMap that GeoJsonFeature objects are being placed on
     *
     * @return GoogleMap
     */
    /* package */ GoogleMap getMap() {
        return mMap;
    }

    /**
     * Changes the map that GeoJsonFeature objects are being drawn onto. Existing objects are
     * removed from the previous map and drawn onto the new map.
     *
     * @param map GoogleMap to place GeoJsonFeature objects on
     */
    /* package */ void setMap(GoogleMap map) {
        for (GeoJsonFeature feature : mFeatures.keySet()) {
            redrawFeatureToMap(feature, map);
        }
    }

    /**
     * Gets a set containing GeoJsonFeatures
     *
     * @return set containing GeoJsonFeatures
     */
    /* package */ Set<GeoJsonFeature> getFeatures() {
        return mFeatures.keySet();
    }

    /**
     * Adds a new GeoJsonFeature to the map if its geometry property is not null.
     *
     * @param feature feature to add to the map
     */
    /* package */ void addFeature(GeoJsonFeature feature) {
        feature.addObserver(this);
        Object mapObject = null;
        if (feature.hasGeometry()) {
            mapObject = addFeatureToMap(feature, feature.getGeometry());
        }
        mFeatures.put(feature, mapObject);
    }

    /**
     * Removes all GeoJsonFeature objects stored in the mFeatures hashmap from the map
     */
    /* package */ void removeLayerFromMap() {
        for (GeoJsonFeature feature : mFeatures.keySet()) {
            removeFeature(feature);
        }
    }

    /**
     * Removes a GeoJsonFeature from the map if its geometry property is not null
     *
     * @param feature feature to remove from map
     */
    /* package */ void removeFeature(GeoJsonFeature feature) {
        // Check if given feature is stored
        if (mFeatures.containsKey(feature)) {
            removeFromMap(mFeatures.remove(feature));
            feature.deleteObserver(this);
        }
    }

    /**
     * Adds a new object onto the map using the GeoJsonGeometry for the coordinates and the
     * GeoJsonFeature for the styles.
     *
     * @param geoJsonFeature  feature to get geometry style
     * @param geoJsonGeometry geometry to add to the map
     */
    private Object addFeatureToMap(GeoJsonFeature geoJsonFeature, GeoJsonGeometry geoJsonGeometry) {
        String geometryType = geoJsonGeometry.getType();
        if (geometryType.equals("Point")) {
            return addPointToMap(geoJsonFeature.getPointStyle(), (GeoJsonPoint) geoJsonGeometry);
        } else if (geometryType.equals("LineString")) {
            return addLineStringToMap(geoJsonFeature.getLineStringStyle(),
                    (GeoJsonLineString) geoJsonGeometry);
        } else if (geometryType.equals("Polygon")) {
            return addPolygonToMap(geoJsonFeature.getPolygonStyle(),
                    (GeoJsonPolygon) geoJsonGeometry);
        } else if (geometryType.equals("MultiPoint")) {
            return addMultiPointToMap(geoJsonFeature.getPointStyle(),
                    (GeoJsonMultiPoint) geoJsonGeometry);
        } else if (geometryType.equals("MultiLineString")) {
            return addMultiLineStringToMap(geoJsonFeature.getLineStringStyle(),
                    ((GeoJsonMultiLineString) geoJsonGeometry));
        } else if (geometryType.equals("MultiPolygon")) {
            return addMultiPolygonToMap(geoJsonFeature.getPolygonStyle(),
                    ((GeoJsonMultiPolygon) geoJsonGeometry));
        } else if (geometryType.equals("GeometryCollection")) {
            return addGeometryCollectionToMap(geoJsonFeature,
                    ((GeoJsonGeometryCollection) geoJsonGeometry).getGeometries());
        }
        return null;
    }

    /**
     * Adds a GeoJsonPoint to the map as a Marker
     *
     * @param geoJsonPointStyle contains relevant styling properties for the Marker
     * @param geoJsonPoint      contains coordinates for the Marker
     * @return Marker object created from the given GeoJsonPoint
     */
    private Marker addPointToMap(GeoJsonPointStyle geoJsonPointStyle, GeoJsonPoint geoJsonPoint) {
        MarkerOptions markerOptions = geoJsonPointStyle.getMarkerOptions();
        markerOptions.position(geoJsonPoint.getCoordinates());
        return mMap.addMarker(markerOptions);
    }

    /**
     * Adds all GeoJsonPoint objects in GeoJsonMultiPoint to the map as multiple Markers
     *
     * @param geoJsonPointStyle contains relevant styling properties for the Markers
     * @param geoJsonMultiPoint contains an array of GeoJsonPoints
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(GeoJsonPointStyle geoJsonPointStyle,
            GeoJsonMultiPoint geoJsonMultiPoint) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (GeoJsonPoint geoJsonPoint : geoJsonMultiPoint.getPoints()) {
            markers.add(addPointToMap(geoJsonPointStyle, geoJsonPoint));
        }
        return markers;
    }

    /**
     * Adds a GeoJsonLineString to the map as a Polyline
     *
     * @param geoJsonLineStringStyle contains relevant styling properties for the Polyline
     * @param geoJsonLineString      contains coordinates for the Polyline
     * @return Polyline object created from given GeoJsonLineString
     */
    private Polyline addLineStringToMap(GeoJsonLineStringStyle geoJsonLineStringStyle,
            GeoJsonLineString geoJsonLineString) {
        PolylineOptions polylineOptions = geoJsonLineStringStyle.getPolylineOptions();
        // Add coordinates
        polylineOptions.addAll(geoJsonLineString.getCoordinates());
        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds all GeoJsonLineString objects in the GeoJsonMultiLineString to the map as multiple
     * Polylines
     *
     * @param geoJsonLineStringStyle contains relevant styling properties for the Polylines
     * @param geoJsonMultiLineString contains an array of GeoJsonLineStrings
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(
            GeoJsonLineStringStyle geoJsonLineStringStyle,
            GeoJsonMultiLineString geoJsonMultiLineString) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        for (GeoJsonLineString geoJsonLineString : geoJsonMultiLineString.getLineStrings()) {
            polylines.add(addLineStringToMap(geoJsonLineStringStyle, geoJsonLineString));
        }
        return polylines;
    }

    /**
     * Adds a GeoJsonPolygon to the map as a Polygon
     *
     * @param geoJsonPolygonStyle contains relevant styling properties for the Polygon
     * @param geoJsonPolygon      contains coordinates for the Polygon
     * @return Polygon object created from given GeoJsonPolygon
     */
    private Polygon addPolygonToMap(GeoJsonPolygonStyle geoJsonPolygonStyle,
            GeoJsonPolygon geoJsonPolygon) {
        PolygonOptions polygonOptions = geoJsonPolygonStyle.getPolygonOptions();
        // First array of coordinates are the outline
        polygonOptions.addAll(geoJsonPolygon.getCoordinates().get(POLYGON_OUTER_COORDINATE_INDEX));
        // Following arrays are holes
        for (int i = POLYGON_INNER_COORDINATE_INDEX; i < geoJsonPolygon.getCoordinates().size();
                i++) {
            polygonOptions.addHole(geoJsonPolygon.getCoordinates().get(i));
        }
        return mMap.addPolygon(polygonOptions);
    }

    /**
     * Adds all GeoJsonPolygon in the GeoJsonMultiPolygon to the map as multiple Polygons
     *
     * @param geoJsonPolygonStyle contains relevant styling properties for the Polygons
     * @param geoJsonMultiPolygon contains an array of GeoJsonPolygons
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<Polygon> addMultiPolygonToMap(GeoJsonPolygonStyle geoJsonPolygonStyle,
            GeoJsonMultiPolygon geoJsonMultiPolygon) {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (GeoJsonPolygon geoJsonPolygon : geoJsonMultiPolygon.getPolygons()) {
            polygons.add(addPolygonToMap(geoJsonPolygonStyle, geoJsonPolygon));
        }
        return polygons;
    }

    /**
     * Adds all GeoJsonGeometry objects stored in the GeoJsonGeometryCollection onto the map.
     * Supports recursive GeometryCollections.
     *
     * @param geoJsonFeature    contains relevant styling properties for the GeoJsonGeometry inside
     *                          the GeoJsonGeometryCollection
     * @param geoJsonGeometries contains an array of GeoJsonGeometry objects
     * @return array of Marker, Polyline, Polygons that have been added to the map
     */
    private ArrayList<Object> addGeometryCollectionToMap(GeoJsonFeature geoJsonFeature,
            ArrayList<GeoJsonGeometry> geoJsonGeometries) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        for (GeoJsonGeometry geometry : geoJsonGeometries) {
            geometries.add(addFeatureToMap(geoJsonFeature, geometry));
        }
        return geometries;
    }

    /**
     * Redraws a given GeoJsonFeature onto the map. The map object is obtained from the mFeatures
     * hashmap and it is removed and added.
     *
     * @param feature feature to redraw onto the map
     */
    private void redrawFeatureToMap(GeoJsonFeature feature) {
        redrawFeatureToMap(feature, mMap);
    }

    private void redrawFeatureToMap(GeoJsonFeature feature, GoogleMap map) {
        removeFromMap(mFeatures.get(feature));
        mFeatures.put(feature, FEATURE_NOT_ON_MAP_VALUE);
        mMap = map;
        if (map != null && feature.hasGeometry()) {
            mFeatures.put(feature, addFeatureToMap(feature, feature.getGeometry()));
        }
    }

    /**
     * Update is called if the developer sets a style or geometry in a GeoJsonFeature object
     *
     * @param observable GeoJsonFeature object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonFeature) {
            GeoJsonFeature feature = ((GeoJsonFeature) observable);
            boolean featureIsOnMap = mFeatures.get(feature) != FEATURE_NOT_ON_MAP_VALUE;
            if (featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature has been added to the map and its geometry is not null
                // TODO: change this so that we don't add and remove
                redrawFeatureToMap(feature);
            } else if (featureIsOnMap && !feature.hasGeometry()) {
                // Checks if feature is on map and geometry is null
                removeFromMap(mFeatures.get(feature));
                mFeatures.put(feature, FEATURE_NOT_ON_MAP_VALUE);
            } else if (featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature isn't on the map and geometry is not null
                addFeature(feature);
            }
        }
    }
}
