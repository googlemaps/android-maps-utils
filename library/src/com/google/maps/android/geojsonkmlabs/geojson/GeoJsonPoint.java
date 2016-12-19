package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojsonkmlabs.Point;

/**
 * A GeoJsonPoint geometry contains a single {@link com.google.android.gms.maps.model.LatLng}.
 */
public class GeoJsonPoint extends Point {

    /**
     * Creates a new GeoJsonPoint
     *
     * @param coordinates coordinates of GeoJsonPoint to store
     */
    public GeoJsonPoint(LatLng coordinates) {
        super(coordinates);
    }

    /** {@inheritDoc} */
    public String getType() {
        return getGeometryType();
    }

    /**
     * Gets the coordinates of the GeoJsonPoint
     *
     * @return coordinates of the GeoJsonPoint
     */
    public LatLng getCoordinates() {
        return super.mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}