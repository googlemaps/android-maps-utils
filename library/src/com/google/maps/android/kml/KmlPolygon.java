package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Represents a KML Polygon. Contains a single array of outer boundary coordinates and an array of
 * arrays for the inner boundary coordinates.
 */
public class KmlPolygon implements KmlGeometry<ArrayList<ArrayList<LatLng>>> {

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
    public String getGeometryType() {
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
    public ArrayList<ArrayList<LatLng>> getGeometryObject() {
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(mOuterBoundaryCoordinates);
        //Polygon objects do not have to have inner holes
        if (mInnerBoundaryCoordinates != null) {
            coordinates.addAll(mInnerBoundaryCoordinates);
        }
        return coordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n outer coordinates=").append(mOuterBoundaryCoordinates);
        sb.append(",\n inner coordinates=").append(mInnerBoundaryCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}
