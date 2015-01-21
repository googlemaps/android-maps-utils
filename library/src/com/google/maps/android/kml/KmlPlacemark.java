package com.google.maps.android.kml;

import java.util.HashMap;

/**
 * Represents a placemark which is either a {@link com.google.maps.android.kml.KmlPoint}, {@link
 * com.google.maps.android.kml.KmlLineString}, {@link com.google.maps.android.kml.KmlPolygon} or a
 * {@link com.google.maps.android.kml.KmlMultiGeometry}. Stores the properties and styles of the
 * placemark.
 */
public class KmlPlacemark {

    private final KmlGeometry mGeometry;

    private final String mStyle;

    private HashMap<String, String> mPlacemarkProperties;

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
    public String getStyleID() {
        return mStyle;
    }

    /**
     * Gets the property entry set
     *
     * @return property entry set
     */
    public Iterable getProperties() {
        return mPlacemarkProperties.entrySet();
    }

    public String getProperty(String keyValue) {
        return mPlacemarkProperties.get(keyValue);
    }

    /**
     * Gets the geometry object
     *
     * @return geometry object
     */
    public KmlGeometry getGeometry() {
        return mGeometry;
    }

    /**
     * Gets whether the placemark has a given property
     *
     * @param keyValue key value to check
     * @return true if the key is stored in the properties, false otherwise
     */
    public boolean hasProperty(String keyValue) {
        return mPlacemarkProperties.containsKey(keyValue);
    }

}
