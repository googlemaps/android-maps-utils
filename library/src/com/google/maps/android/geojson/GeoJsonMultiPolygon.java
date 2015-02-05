package com.google.maps.android.geojson;

import java.util.List;

/**
 * A GeoJsonMultiPolygon geometry contains a number of {@link GeoJsonPolygon}s.
 */
public class GeoJsonMultiPolygon implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "MultiPolygon";

    private final List<GeoJsonPolygon> mGeoJsonPolygons;

    /**
     * Creates a new GeoJsonMultiPolygon
     *
     * @param geoJsonPolygons list of GeoJsonPolygons to store
     */
    public GeoJsonMultiPolygon(List<GeoJsonPolygon> geoJsonPolygons) {
        if (geoJsonPolygons == null) {
            throw new IllegalArgumentException("GeoJsonPolygons cannot be null");
        }
        mGeoJsonPolygons = geoJsonPolygons;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of GeoJsonPolygons
     *
     * @return list of GeoJsonPolygons
     */
    public List<GeoJsonPolygon> getPolygons() {
        return mGeoJsonPolygons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n Polygons=").append(mGeoJsonPolygons);
        sb.append("\n}\n");
        return sb.toString();
    }
}
