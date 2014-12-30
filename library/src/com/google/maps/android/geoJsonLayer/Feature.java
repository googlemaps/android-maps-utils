package com.google.maps.android.geoJsonLayer;

import java.util.Map;

/**
 * Created by juliawong on 12/29/14.
 */
public class Feature {

    private Geometry mGeometry;

    private Style mStyle;

    private String mId;

    private Map<String, String> mProperties;

    // check if ID exists

    /**
     * Creates a new Feature object
     *
     * @param geometry   type of geometry to assign to the feature
     * @param id         id to refer to the feature by
     * @param properties map of data containing properties related to the feature
     */
    public Feature(Geometry geometry, String id, Map<String, String> properties) {
        this.mGeometry = geometry;
        this.mId = id;
        this.mProperties = properties;
    }

    // TODO: implement an iterator thing or just return mProperties

    /**
     * Get the style of the feature
     *
     * @return style object
     */
    public Style getStyle() {
        return mStyle;
    }

    // TODO: Redraw with new style and validate style against geometry

    /**
     * Sets the style of the feature, this will override default styles set in Collection
     *
     * @param style new style to set for this feature
     * @return the previous style that the new style has overwritten
     */
    public Style setStyle(Style style) {
        Style oldStyle = mStyle;
        mStyle = style;
        return oldStyle;
    }

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    public Geometry getGeometry() {
        return mGeometry;
    }

    /**
     * Gets the ID of the feature
     *
     * @return ID of the feature
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets a property with an assigned value in the properties map
     *
     * @param property key value to add to property map
     * @param value    value related to key to add
     * @return previous value of property if overwritten, otherwise null
     */
    public String setProperty(String property, String value) {
        return mProperties.put(property, value);
    }

    /**
     * Gets a property value from a given property key
     *
     * @param property key of property to get from properties map
     * @return value of property if found, otherwise null
     */
    public String getProperty(String property) {
        return mProperties.get(property);
    }

    /**
     * Removes a property key and value from the properties map
     *
     * @param property key of property to remove from the properties map
     * @return value of property removed if it was found, otherwise null
     */
    public String removeProperty(String property) {
        return mProperties.remove(property);
    }
}
