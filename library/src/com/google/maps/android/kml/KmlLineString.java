package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a KML LineString. Contains a single array of coordinates.
 */
public class KmlLineString implements KmlGeometry<List<LatLng>>, KmlContainsLocation {

    public static final String GEOMETRY_TYPE = "LineString";

    final ArrayList<LatLng> mCoordinates;

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(ArrayList<LatLng> coordinates) {
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
    @Override
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of LatLng
     */
    public ArrayList<LatLng> getGeometryObject() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }

    /**
     * Checks if the given point lies on the path,
     * using the PolyUtil.isLocationOnPath method.
     * @param point
     * @param geodesic
     * @return
     */
    @Override
    public boolean containsLocation(LatLng point, boolean geodesic) {
        return PolyUtil.isLocationOnPath(point, mCoordinates, geodesic, 0);
    }
}
