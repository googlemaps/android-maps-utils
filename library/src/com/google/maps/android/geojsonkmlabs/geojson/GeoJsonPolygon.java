package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojsonkmlabs.Geometry;

import java.util.List;

/**
 * A GeoJsonPolygon geometry contains an array of arrays of {@link com.google.android.gms.maps.model.LatLng}s.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */

public class GeoJsonPolygon implements Geometry {

    private final static String GEOMETRY_TYPE = "Polygon";

    private final List<? extends List<LatLng>> mCoordinates;

    /**
     * Creates a new GeoJsonPolygon object
     *
     * @param coordinates list of list of coordinates of GeoJsonPolygon to store
     */
    public GeoJsonPolygon(
            List<? extends List<LatLng>> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of a list of coordinates of the GeoJsonPolygons
     *
     * @return list of a list of coordinates of the GeoJsonPolygon
     */
    public List<? extends List<LatLng>> getCoordinates() {
        return mCoordinates;
    }

    //todo this is not v good
    public List<? extends List<LatLng>> getGeometryObject() { return getCoordinates(); }

    public String getGeometryType() { return getType(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}
