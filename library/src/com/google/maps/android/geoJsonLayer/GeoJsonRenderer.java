package com.google.maps.android.geoJsonLayer;

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

/**
 * Created by lavenderch on 1/8/15.
 */
public class GeoJsonRenderer implements Observer {

    private final static int POLYGON_OUTER_COORDINATES = 0;

    private final static int POLYGON_INNER_COORDINATES = 1;

    private final static Object NOT_ON_MAP = null;

    private final HashMap<GeoJsonFeature, Object> mFeatures;

    private GoogleMap mMap;

    public GeoJsonRenderer(HashMap<GeoJsonFeature, Object> features, GoogleMap map) {
        mFeatures = features;
        mMap = map;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Removes all feature objects from the current map, then readds feature objects to the new map
     */
    public void setMap(GoogleMap map) {
        removeLayerFromMap();
        mMap = map;
        addLayerToMap();
    }

    /**
     * Adds all features which are available in this collection to the map
     */
    public void addLayerToMap() {
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            addFeature(geoJsonFeature);
        }
    }

    /**
     * Adds a new GeoJsonFeature to the map
     *
     * @param feature feature to add to the map
     */
    public void addFeature(GeoJsonFeature feature) {
        if (feature.hasGeometry()) {
            addFeatureToMap(feature, feature.getGeometry());
        }
    }

    /**
     * Helper function to remove all GeoJson objects from the current map as well as from the hashmap
     */
    public void removeLayerFromMap() {
        for (GeoJsonFeature feature : mFeatures.keySet()) {
            removeFeature(feature);
        }
    }

    public void removeFeature(GeoJsonFeature feature) {
        removeFromMap(mFeatures.get(feature));
    }

    /**
     * Adds all objects currently stored in the mFeature array, onto the map
     *
     * @param geoJsonFeature  feature to get geometry style
     * @param geoJsonGeometry Geometry to add to the map
     */
    private void addFeatureToMap(GeoJsonFeature geoJsonFeature, GeoJsonGeometry geoJsonGeometry) {
        String geometryType = geoJsonGeometry.getType();
        if (geometryType.equals("Point")) {
            mFeatures.put(geoJsonFeature,
                    addPointToMap(geoJsonFeature.getPointStyle(), (GeoJsonPoint) geoJsonGeometry));
        } else if (geometryType.equals("LineString")) {
            mFeatures.put(geoJsonFeature, addLineStringToMap(geoJsonFeature.getLineStringStyle(),
                    (GeoJsonLineString) geoJsonGeometry));
        } else if (geometryType.equals("Polygon")) {
            mFeatures.put(geoJsonFeature, addPolygonToMap(geoJsonFeature.getPolygonStyle(),
                    (GeoJsonPolygon) geoJsonGeometry));
        } else if (geometryType.equals("MultiPoint")) {
            mFeatures.put(geoJsonFeature, addMultiPointToMap(geoJsonFeature));
        } else if (geometryType.equals("MultiLineString")) {
            mFeatures.put(geoJsonFeature, addMultiLineStringToMap(geoJsonFeature));
        } else if (geometryType.equals("MultiPolygon")) {
            mFeatures.put(geoJsonFeature, addMultiPolygonToMap(geoJsonFeature));
        } else if (geometryType.equals("GeometryCollection")) {
            GeoJsonGeometryCollection geometryCollection
                    = ((GeoJsonGeometryCollection) geoJsonFeature.getGeometry());
            ArrayList<Object> geometries = new ArrayList<Object>();
            mFeatures.put(geoJsonFeature,
                    addGeometryCollectionToMap(geometryCollection, geometries, geoJsonFeature));
        }
    }

    /**
     * Goes through a geometry collection and retrieves geometries to put to the map. If another
     * geometry collection is detected, then it recursively goes through the list of geometries
     * until no more geometry collections are found
     *
     * @param geoJsonGeometry geometry collection we wish to retrieve geometry objects from
     * @param geometries      an arraylist of geometries that have been added to the map
     * @param feature         class to retrieves styles from
     * @return an arraylist of geometries which have been added to the map
     */
    private ArrayList<Object> addGeometryCollectionToMap(GeoJsonGeometryCollection geoJsonGeometry,
            ArrayList<Object> geometries, GeoJsonFeature feature) {
        for (GeoJsonGeometry geometry : geoJsonGeometry.getGeometries()) {
            if (geometry.getType().equals("GeometryCollection")) {
                addGeometryCollectionToMap((GeoJsonGeometryCollection) geometry, geometries,
                        feature);
            } else {
                String geometryType = geometry.getType();
                if (geometryType.equals("Point")) {
                    geometries.add(addPointToMap(feature.getPointStyle(), (GeoJsonPoint) geometry));
                } else if (geometryType.equals("LineString")) {
                    geometries.add(addLineStringToMap(feature.getLineStringStyle(),
                            (GeoJsonLineString) geometry));
                } else if (geometryType.equals("Polygon")) {
                    geometries.add(addPolygonToMap(feature.getPolygonStyle(),
                            (GeoJsonPolygon) geometry));
                } else if (geometryType.equals("MultiPoint")) {
                    geometries.add(addMultiPointToMap(feature));
                } else if (geometryType.equals("MultiLineString")) {
                    geometries.add(addMultiLineStringToMap(feature));
                } else if (geometryType.equals("MultiPolygon")) {
                    geometries.add(addMultiPolygonToMap(feature));
                }
            }
        }
        return geometries;
    }

    /**
     * Adds a Point to the map as a Marker
     *
     * @param geoJsonPointStyle contains relevant styling properties for the Marker
     * @param geoJsonPoint      contains coordinates for the Marker
     * @return Marker object created from the given point feature
     */
    private Marker addPointToMap(GeoJsonPointStyle geoJsonPointStyle, GeoJsonPoint geoJsonPoint) {
        MarkerOptions markerOptions = geoJsonPointStyle.getMarkerOptions();
        markerOptions.position(geoJsonPoint.getCoordinates());
        return mMap.addMarker(markerOptions);
    }

    /**
     * Adds all Points in MultiPoint to the map as multiple Markers
     *
     * @param geoJsonFeature contains MultiPoint and relevant style properties
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(GeoJsonFeature geoJsonFeature) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (GeoJsonPoint geoJsonPoint : ((GeoJsonMultiPoint) geoJsonFeature.getGeometry())
                .getPoints()) {
            markers.add(addPointToMap(geoJsonFeature.getPointStyle(), geoJsonPoint));
        }
        return markers;
    }

    /**
     * Adds a LineString to the map as a Polyline
     *
     * @param geoJsonLineStringStyle contains relevant styling properties for the Polyline
     * @param geoJsonLineString      contains coordinates for the Polyline
     * @return Polyline object created from given feature
     */
    private Polyline addLineStringToMap(GeoJsonLineStringStyle geoJsonLineStringStyle,
            GeoJsonLineString geoJsonLineString) {
        PolylineOptions polylineOptions = geoJsonLineStringStyle.getPolylineOptions();
        // Add coordinates
        polylineOptions.addAll(geoJsonLineString.getCoordinates());
        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds all LineStrings in the MultiLineString to the map as multiple Polylines
     *
     * @param geoJsonFeature contains MultiLineString and relevant style properties
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(GeoJsonFeature geoJsonFeature) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        for (GeoJsonLineString geoJsonLineString : ((GeoJsonMultiLineString) geoJsonFeature
                .getGeometry()).getLineStrings()) {
            polylines.add(addLineStringToMap(geoJsonFeature.getLineStringStyle(),
                    geoJsonLineString));
        }
        return polylines;
    }

    /**
     * Adds a GeoJSON Polygon to the map as a Polygon
     *
     * @param geoJsonPolygonStyle contains relevant styling properties for the Polygon
     * @param geoJsonPolygon      contains coordinates for the Polygon
     * @return Polygon object created from given feature
     */
    private Polygon addPolygonToMap(GeoJsonPolygonStyle geoJsonPolygonStyle,
            GeoJsonPolygon geoJsonPolygon) {
        PolygonOptions polygonOptions = geoJsonPolygonStyle.getPolygonOptions();
        // First array of coordinates are the outline
        polygonOptions.addAll(geoJsonPolygon.getCoordinates().get(POLYGON_OUTER_COORDINATES));
        // Following arrays are holes
        for (int i = POLYGON_INNER_COORDINATES; i < geoJsonPolygon.getCoordinates().size(); i++) {
            polygonOptions.addHole(geoJsonPolygon.getCoordinates().get(i));
        }
        return mMap.addPolygon(polygonOptions);
    }


    /**
     * Adds all GeoJSON Polygons in the MultiPolygon to the map as multiple Polygons
     *
     * @param geoJsonFeature contains MultiPolygon and relevant style properties
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<Polygon> addMultiPolygonToMap(GeoJsonFeature geoJsonFeature) {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (GeoJsonPolygon geoJsonPolygon : ((GeoJsonMultiPolygon) geoJsonFeature.getGeometry())
                .getPolygons()) {
            polygons.add(addPolygonToMap(geoJsonFeature.getPolygonStyle(), geoJsonPolygon));
        }
        return polygons;
    }

    /**
     * Gets a Marker, Polyline, Polygon or an array of these and removes from the map
     *
     * @param mapObject map object to remove from the map
     */
    private void removeFromMap(Object mapObject) {
        if (mapObject instanceof Marker) {
            ((Marker) mapObject).remove();
        } else if (mapObject instanceof Polyline) {
            ((Polyline) mapObject).remove();
        } else if (mapObject instanceof Polygon) {
            ((Polygon) mapObject).remove();
        }  else if (mapObject instanceof ArrayList) {
            for (Object mapObjectElement : (ArrayList<Object>) mapObject) {
                removeFromMap(mapObjectElement);
            }
        }
    }

    /**
     * Given a feature, removes all instances of that feature from the current map and redraws it
     * again.
     *
     * @param feature Feature to redraw to the map
     */
    private void redrawCollectionToMap(GeoJsonFeature feature, Object googleMapObject) {
        removeFromMap(googleMapObject);
        mFeatures.put(feature, NOT_ON_MAP);
        addFeatureToMap(feature, feature.getGeometry());
    }

    /**
     * Update is called if the developer sets a style in a Feature object
     *
     * @param observable Feature object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonFeature) {
            GeoJsonFeature geoJsonFeature = ((GeoJsonFeature) observable);
            if (mFeatures.get(geoJsonFeature) != NOT_ON_MAP) {
                redrawCollectionToMap(geoJsonFeature, mFeatures.get(geoJsonFeature));
            } else {
                // Adds new features to the map
                // e.g. feature prev had geom defined as null, now a point
                addFeature(geoJsonFeature);
            }

        }

    }
}
