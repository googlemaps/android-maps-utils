package com.google.maps.android.geojsonkmlabs;

import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonPointStyle;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonPolygonStyle;

import java.util.HashMap;
import java.util.Observable;

/**
 * Created by suvercha on 12/15/16.
 */

public class Feature extends Observable {
    private final String mId;

    private final HashMap<String, String> mProperties;

    private Geometry mGeometry;

    private GeoJsonPointStyle mPointStyle;

    private GeoJsonLineStringStyle mLineStringStyle;

    private GeoJsonPolygonStyle mPolygonStyle;


    public Feature(Geometry featureGeometry, String id,
                          HashMap<String, String> properties){
        mGeometry = featureGeometry;
        mId = id;
        if (properties == null) {
            mProperties = new HashMap<String, String>();
        } else {
            mProperties = properties;
        }
        //leaving bounding box out for now
    }

    /**
     * Returns all the stored property keys
     *
     * @return iterable of property keys
     */
    public Iterable<String> getPropertyKeys() {
        return mProperties.keySet();
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
     * Gets the value for a stored property
     *
     * @param property key of the property
     * @return value of the property if its key exists, otherwise null
     */
    public String getProperty(String property) {
        return mProperties.get(property);
    }


    public String getId() {
        return mId;
    }

    /**
     * Checks whether the given property key exists
     *
     * @param property key of the property to check
     * @return true if property key exists, false otherwise
     */
    public boolean hasProperty(String property) {
        return mProperties.containsKey(property);
    }

    /**
     * Gets the geometry object
     *
     * @return geometry object
     */
    public Geometry getGeometry() {
        return mGeometry;
    }

    /**
     * Gets whether the placemark has a properties
     *
     * @return true if there are properties in the properties hashmap, false otherwise
     */
    public boolean hasProperties() {
        return mProperties.size() > 0;
    }

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, otherwise null
     */
    public boolean hasGeometry() {
        return (mGeometry != null);
    }


}
