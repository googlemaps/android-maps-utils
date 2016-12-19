package com.google.maps.android.geojsonkmlabs.kml;

/**
 * Represents a KML geometry object.
 *
 * @param <T> type of object that the coordinates are stored in
 */
public interface KmlGeometry<T> {

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