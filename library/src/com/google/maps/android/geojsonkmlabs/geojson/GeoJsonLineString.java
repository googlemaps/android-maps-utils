package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * A GeoJsonLineString geometry represents a number of connected {@link
 * com.google.android.gms.maps.model.LatLng}s.
 */
public class GeoJsonLineString implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "LineString";

    private final List<LatLng> mCoordinates;

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates list of coordinates of GeoJsonLineString to store
     */
    public GeoJsonLineString(List<LatLng> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates of the GeoJsonLineString
     *
     * @return list of coordinates of the GeoJsonLineString
     */
    public List<LatLng> getCoordinates() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}
