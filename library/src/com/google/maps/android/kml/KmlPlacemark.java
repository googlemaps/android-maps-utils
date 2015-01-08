package com.google.maps.android.kml;

import java.util.HashMap;

/**
 * Created by lavenderc on 12/3/14.
 *
 * Represents a placemark which is either a point, linestring, polygon or multigeometry
 * Stores the properties about the placemark including coordinates
 */
public class KmlPlacemark {

    private HashMap<String, String> mPlacemarkProperties;

    private final KmlGeometry mGeometry;

    private final String mStyle;

    /**
     * Creates a new KmlPlacemark object
     *
     * @param geometry   geometry object to store
     * @param style      style id to store
     * @param properties properties hashmap to store
     */
    public KmlPlacemark(KmlGeometry geometry, String style, HashMap<String, String> properties) {
        mPlacemarkProperties = new HashMap<String, String>();
        mGeometry = geometry;
        mStyle = style;
        mPlacemarkProperties = properties;
    }

    /**
     * Gets the style id associated with the placemark
     *
     * @return style id
     */
    public String getStyle() {
        return mStyle;
    }

    /**
     * Gets the properties hashmap
     *
     * @return properties hashmap
     */
    public HashMap<String, String> getProperties() {
        return mPlacemarkProperties;
    }

    /**
     * Gets the geometry object
     *
     * @return geometry object
     */
    public KmlGeometry getGeometry() {
        return mGeometry;
    }

}
