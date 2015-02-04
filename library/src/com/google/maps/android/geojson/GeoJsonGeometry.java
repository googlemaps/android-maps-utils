package com.google.maps.android.geojson;

/**
 * Represents a GeoJSON geometry object.
 */
public interface GeoJsonGeometry {

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type' specification.
     *
     * @return type of geometry
     */
    public String getType();
}
