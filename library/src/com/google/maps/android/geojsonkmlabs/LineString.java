package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/*
An abstraction that shares the common properties of KmlLineString and GeoJsonLineString
 */
public class LineString implements Geometry<List<LatLng>> {

    private static final String GEOMETRY_TYPE = "LineString";

    private final List<LatLng> mCoordinates;

    /**
     * Creates a new LineString object
     *
     * @param coordinates array of coordinates
     */
    public LineString(List<LatLng> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType() { return GEOMETRY_TYPE; }

    /**
     * Gets the coordinates of the LineString
     *
     * @return coordinates of the LineString
     */
    public List<LatLng> getGeometryObject() {
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
