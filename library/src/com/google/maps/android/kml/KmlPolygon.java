package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Represents a series of coordinates in a placemark
 */
public class KmlPolygon implements KmlGeometry {

    public static final String GEOMETRY_TYPE = "Polygon";

    private final ArrayList<LatLng> mOuterBoundaryCoordinates;

    private final ArrayList<ArrayList<LatLng>> mInnerBoundaryCoordinates;

    /**
     * Creates a new KmlPolygon object
     *
     * @param outerBoundaryCoordinates single array of outer boundary coordinates
     * @param innerBoundaryCoordinates multiple arrays of inner boundary coordinates
     */
    public KmlPolygon(ArrayList<LatLng> outerBoundaryCoordinates,
            ArrayList<ArrayList<LatLng>> innerBoundaryCoordinates) {
        if (outerBoundaryCoordinates == null) {
            throw new IllegalArgumentException("Outer boundary coordinates cannot be null");
        } else {
            mOuterBoundaryCoordinates = outerBoundaryCoordinates;
            mInnerBoundaryCoordinates = innerBoundaryCoordinates;
        }
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getKmlGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets an array of outer boundary coordinates
     *
     * @return array of outer boundary coordinates
     */
    public ArrayList<LatLng> getOuterBoundaryCoordinates() {
        return mOuterBoundaryCoordinates;
    }

    /**
     * Gets an array of arrays of inner boundary coordinates
     *
     * @return array of arrays of inner boundary coordinates
     */
    public ArrayList<ArrayList<LatLng>> getInnerBoundaryCoordinates() {
        return mInnerBoundaryCoordinates;
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of an ArrayList of LatLng points
     */
    public Object getKmlGeometryCoordinates() {
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(mOuterBoundaryCoordinates);
        coordinates.addAll(mInnerBoundaryCoordinates);
        return coordinates;
    }

}
