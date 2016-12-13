package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;

/**
 * Represents a KML Polygon. Contains a single array of outer boundary coordinates and an array of
 * arrays for the inner boundary coordinates.
 */
public class KmlPolygon implements KmlGeometry<ArrayList<ArrayList<LatLng>>>, KmlContainsLocation {

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

    /**
     * Checks if the given point lies inside of the polygon, using the
     * PolyUtil.containsLocation method.
     * The polygon is formed of great circle segments if geodesic is true, and of rhumb
     * (loxodromic) segments otherwise.
     *
     * @param point
     * @param geodesic Parameter for the PolyUtil.containsLocation method.
     * @return true if the point is inside of the polygon.
     */
    @Override
    public boolean containsLocation(LatLng point, boolean geodesic) {
        if (point == null) return false;

        boolean isInOuter = PolyUtil.containsLocation(point, mOuterBoundaryCoordinates, geodesic);
        // False if the location isn't in the outer bounds of the polygon.
        if (!isInOuter) return false;

        // Check if there any inner areas.
        if (mInnerBoundaryCoordinates != null && mInnerBoundaryCoordinates.size() > 0) {
            for (ArrayList<LatLng> innerArea : mInnerBoundaryCoordinates) {
                // False if the point lies in any of the inner areas ("holes").
                if (PolyUtil.containsLocation(point, innerArea, geodesic)) {
                    return false;
                }
            }
        }

        // The point lies inside the outer bounds and there are either no inner areas,
        // or it lies in none of them.
        return true;
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
