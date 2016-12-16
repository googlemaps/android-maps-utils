package com.google.maps.android.geojsonkmlabs.geojson;

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
     * @param geoJsonPoints list of GeoJsonPoints to store
     */
    public GeoJsonMultiPoint(List<GeoJsonPoint> geoJsonPoints) {
        if (geoJsonPoints == null) {
            throw new IllegalArgumentException("GeoJsonPoints cannot be null");
        }
        mGeoJsonPoints = geoJsonPoints;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of GeoJsonPoints
     *
     * @return list of GeoJsonPoints
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
