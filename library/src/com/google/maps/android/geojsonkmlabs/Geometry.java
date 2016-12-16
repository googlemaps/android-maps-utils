package com.google.maps.android.geojsonkmlabs;

/**
 * Created by suvercha on 12/14/16.
 */

public interface Geometry<T> {

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType();

    /**
     * Gets the stored KML Geometry object
     *
     * @return geometry object
     */
    public T getGeometryObject();

}
