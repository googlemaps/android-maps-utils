package com.google.maps.android.geojson;

import java.util.List;

/**
 * A GeoJsonMultiLineString geometry contains a number of {@link GeoJsonLineString}s.
 */
public class GeoJsonMultiLineString implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "MultiLineString";

    private final List<GeoJsonLineString> mGeoJsonLineStrings;

    /**
     * Creates a new GeoJsonMultiLineString object
     *
     * @param geoJsonLineStrings array of GeoJsonLineStrings to add to the GeoJsonMultiLineString
     */
    public GeoJsonMultiLineString(List<GeoJsonLineString> geoJsonLineStrings) {
        if (geoJsonLineStrings == null) {
            throw new IllegalArgumentException("GeoJsonLineStrings cannot be null");
        }
        mGeoJsonLineStrings = geoJsonLineStrings;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the array of GeoJsonLineString
     *
     * @return array of GeoJsonLineString
     */
    public List<GeoJsonLineString> getLineStrings() {
        return mGeoJsonLineStrings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n LineStrings=").append(mGeoJsonLineStrings);
        sb.append("\n}\n");
        return sb.toString();
    }
}
