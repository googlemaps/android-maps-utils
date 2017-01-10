package com.google.maps.android.geojsonkmlabs;

import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonGeometry;
import com.google.maps.android.geojsonkmlabs.kml.KmlGeometry;

/*
 * An abstraction that shares the common properties of GeoJsonGeometry and KmlGeometry
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
