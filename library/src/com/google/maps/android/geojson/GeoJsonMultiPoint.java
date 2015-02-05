package com.google.maps.android.geojson;

import java.util.List;

/**
 * A GeoJsonMultiPoint geometry contains a number of {@link GeoJsonPoint}s.
 */
public class GeoJsonMultiPoint implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "MultiPoint";

    private final List<GeoJsonPoint> mGeoJsonPoints;

    /**
     * Creates a GeoJsonMultiPoint object
     *
     * @param geoJsonPoints array of GeoJsonPoints to add to the GeoJsonMultiPoint
     */
    public GeoJsonMultiPoint(List<GeoJsonPoint> geoJsonPoints) {
        if (geoJsonPoints == null) {
            throw new IllegalArgumentException("GeoJsonPoints cannot be null");
        }
        mGeoJsonPoints = geoJsonPoints;
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the array of GeoJsonPoint
     *
     * @return array of GeoJsonPoint
     */
    public List<GeoJsonPoint> getPoints() {
        return mGeoJsonPoints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n points=").append(mGeoJsonPoints);
        sb.append("\n}\n");
        return sb.toString();
    }
}
