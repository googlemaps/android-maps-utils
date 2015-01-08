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

    private HashMap<GeoJsonFeature, Object> mFeatures;

    private GoogleMap mMap;

    private final static int POLYGON_OUTER_COORDINATES = 0;

    private final static int POLYGON_INNER_COORDINATES = 1;

    private final static Object NOT_ON_MAP = null;

    public GeoJsonRenderer (HashMap<GeoJsonFeature, Object> features, GoogleMap map) {
        mFeatures = features;
        mMap = map;
    }

    public void setFeatures(HashMap<GeoJsonFeature, Object> features) {
        mFeatures = features;
    }

    public void setMap(GoogleMap map) {
        removeCollectionFromMap();
        mMap = map;
        addCollectionToMap();
    }

    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Adds all features which are available in this collection to the map
     */
    public void addCollectionToMap() {
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            //geometry: "null", is valid - need to check beforehand before we add
            //If mFeatures is null, we haven't added it to the map
            if (geoJsonFeature.hasGeometry()) {
                addToMap(geoJsonFeature, geoJsonFeature.getGeometry());
            }
        }
    }

    private void removeFromMap(GeoJsonFeature feature) {
        if (mFeatures.get(feature) != null) {
            Object googleMapShape = mFeatures.get(feature);
            if (googleMapShape instanceof Polygon) {
                ((Polygon) googleMapShape).remove();
            } else if (googleMapShape instanceof Polyline) {
                ((Polyline) googleMapShape).remove();
            } else if (googleMapShape instanceof Marker) {
                ((Marker) googleMapShape).remove();
            }
            mFeatures.put(feature, null);
        }
    }

    /**
     * Helper function to remove all GeoJson objects from the current map.
     * May or may not want to make this public
     */
    private void removeCollectionFromMap() {
        for (GeoJsonFeature feature : mFeatures.keySet()) {
            removeFromMap(feature);
        }
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
     * Adds all objects currently stored in the mFeature array, onto the map
     *
     * @param geoJsonFeature feature to get geometry style
     * @param GeoJsonGeometry Geometry to add to the map
     */
    private void addToMap(GeoJsonFeature geoJsonFeature, GeoJsonGeometry GeoJsonGeometry) {
        String geometryType = GeoJsonGeometry.getType();
        if (geometryType.equals("Point")) {
            mFeatures.put(geoJsonFeature,
                    addPointToMap(geoJsonFeature.getPointStyle(), (GeoJsonPoint) GeoJsonGeometry));
        } else if (geometryType.equals("LineString")) {
            mFeatures.put(geoJsonFeature, addLineStringToMap(geoJsonFeature.getLineStringStyle(),
                    (GeoJsonLineString) GeoJsonGeometry));
        } else if (geometryType.equals("Polygon")) {
            mFeatures.put(geoJsonFeature, addPolygonToMap(geoJsonFeature.getPolygonStyle(),
                    (GeoJsonPolygon) GeoJsonGeometry));
        } else if (geometryType.equals("MultiPoint")) {
            mFeatures.put(geoJsonFeature, addMultiPointToMap(geoJsonFeature));
        } else if (geometryType.equals("MultiLineString")) {
            mFeatures.put(geoJsonFeature, addMultiLineStringToMap(geoJsonFeature));
        } else if (geometryType.equals("MultiPolygon")) {
            mFeatures.put(geoJsonFeature, addMultiPolygonToMap(geoJsonFeature));
        } else if(geometryType.equals("GeometryCollection")) {
            GeoJsonGeometryCollection geometryCollection = ((GeoJsonGeometryCollection) geoJsonFeature.getGeometry());
            for (GeoJsonGeometry geoJsonGeometryObject : geometryCollection.getGeometries()) {
                addToMap(geoJsonFeature, geoJsonGeometryObject);
            }
        }
    }

    /**
     * Adds all Points in MultiPoint to the map as multiple Markers
     *
     * @param geoJsonFeature contains MultiPoint and relevant style properties
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(GeoJsonFeature geoJsonFeature) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (GeoJsonPoint geoJsonPoint : ((GeoJsonMultiPoint) geoJsonFeature.getGeometry()).getPoints()) {
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
     * Adds all LineStrings in the MultiLineString to the map as multiple Polylines
     *
     * @param geoJsonFeature contains MultiLineString and relevant style properties
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(GeoJsonFeature geoJsonFeature) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        for (GeoJsonLineString geoJsonLineString : ((GeoJsonMultiLineString) geoJsonFeature.getGeometry()).getLineStrings()) {
            polylines.add(addLineStringToMap(geoJsonFeature.getLineStringStyle(),
                    geoJsonLineString));
        }
        return polylines;
    }

    /**
     * Adds all GeoJSON Polygons in the MultiPolygon to the map as multiple Polygons
     *
     * @param geoJsonFeature contains MultiPolygon and relevant style properties
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<Polygon> addMultiPolygonToMap(
            GeoJsonFeature geoJsonFeature) {
        ArrayList<com.google.android.gms.maps.model.Polygon> polygons
                = new ArrayList<com.google.android.gms.maps.model.Polygon>();
        for (GeoJsonPolygon geoJsonPolygon : ((GeoJsonMultiPolygon) geoJsonFeature.getGeometry()).getPolygons()) {
            polygons.add(addPolygonToMap(geoJsonFeature.getPolygonStyle(), geoJsonPolygon));
        }
        return polygons;
    }

    /**
     * Update is called if the developer sets a style in a Feature object
     * @param observable    Feature object
     * @param data          null, no extra argument is passed through the notifyObservers method
     */
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonFeature) {
            GeoJsonFeature feature = ((GeoJsonFeature) observable);
            removeFromMap(feature);
            addToMap(feature, feature.getGeometry());
        }
    }
}
