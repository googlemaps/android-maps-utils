package com.google.maps.android.geojsonkmlabs;

public interface Geometry<T> {

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType();

    /**
     * Gets the stored geometry object
     *
     * @return geometry object
     */
    public T getGeometryObject();

}
