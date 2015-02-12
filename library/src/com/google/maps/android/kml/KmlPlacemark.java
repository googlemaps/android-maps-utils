package com.google.maps.android.kml;

import java.util.HashMap;

/**
 * Represents a placemark which is either a {@link com.google.maps.android.kml.KmlPoint},
 * {@link
 * com.google.maps.android.kml.KmlLineString}, {@link com.google.maps.android.kml.KmlPolygon} or a
 * {@link com.google.maps.android.kml.KmlMultiGeometry}. Stores the properties and styles of the
 * place.
 */
public class KmlPlacemark {

    private final KmlGeometry mGeometry;

    private final String mStyle;

    private final KmlStyle mInlineStyle;

    private HashMap<String, String> mProperties;

    /**
     * Creates a new KmlPlacemark object
     *
     * @param geometry   geometry object to store
     * @param style      style id to store
     * @param properties properties hashmap to store
     */
    public KmlPlacemark(KmlGeometry geometry, String style, KmlStyle inlineStyle,
            HashMap<String, String> properties) {
        mProperties = new HashMap<String, String>();
        mGeometry = geometry;
        mStyle = style;
        mInlineStyle = inlineStyle;
        mProperties = properties;
    }

    /**
     * Gets the style id associated with the basic_placemark
     *
     * @return style id
     */
    public String getStyleId() {
        return mStyle;
    }

    /**
     * Gets the inline style that was found
     *
     * @return InlineStyle or null if not found
     */
    public KmlStyle getInlineStyle() {
        return mInlineStyle;
    }

    /**
     * Gets the property entry set
     *
     * @return property entry set
     */
    public Iterable getProperties() {
        return mProperties.entrySet();
    }

    /**
     * Gets the property based on property name
     *
     * @param keyValue Property name to retrieve value
     * @return property value, null if not found
     */
    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
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
     * Gets whether the basic has a given property
     *
     * @param keyValue key value to check
     * @return true if the key is stored in the properties, false otherwise
     */
    public boolean hasProperty(String keyValue) {
        return mProperties.containsKey(keyValue);
    }

    /**
     * Gets whether the placemark has a properties
     *
     * @return true if there are properties in the properties hashmap, false otherwise
     */
    public boolean hasProperties() {
        return mProperties.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Placemark").append("{");
        sb.append("\n style id=").append(mStyle);
        sb.append(",\n inline style=").append(mInlineStyle);
        sb.append(",\n properties=").append(mProperties);
        sb.append(",\n geometry=").append(mGeometry);
        sb.append("\n}\n");
        return sb.toString();
    }
}
