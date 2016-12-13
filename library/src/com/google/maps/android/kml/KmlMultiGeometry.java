package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Represents a KML MultiGeometry. Contains an array of KmlGeometry objects.
 */
public class KmlMultiGeometry implements KmlGeometry<ArrayList<KmlGeometry>>, KmlContainsLocation {

    private static final String GEOMETRY_TYPE = "MultiGeometry";

    private ArrayList<KmlGeometry> mGeometries = new ArrayList<KmlGeometry>();

    /**
     * Creates a new KmlMultiGeometry object
     *
     * @param geometries array of KmlGeometry objects contained in the MultiGeometry
     */
    public KmlMultiGeometry(ArrayList<KmlGeometry> geometries) {
        if (geometries == null) {
            throw new IllegalArgumentException("Geometries cannot be null");
        }
        mGeometries = geometries;
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
     * Gets an ArrayList of KmlGeometry objects
     *
     * @return ArrayList of KmlGeometry objects
     */

    public ArrayList<KmlGeometry> getGeometryObject() {
        return mGeometries;
    }

    /**
     * Checks if the given point lies inside any of the geometries.
     * A polygon is formed of great circle segments if geodesic is true, and of rhumb
     * (loxodromic) segments otherwise.
     * @param point
     * @param geodesic
     * @return
     */
    @Override
    public boolean containsLocation(LatLng point, boolean geodesic) {
        if (point == null) return false;

        for (KmlGeometry geometry : mGeometries){
            if (geometry instanceof KmlContainsLocation){
                KmlContainsLocation kmlContainsLocation = (KmlContainsLocation)geometry;
                if (kmlContainsLocation.containsLocation(point, geodesic)) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n geometries=").append(mGeometries);
        sb.append("\n}\n");
        return sb.toString();
    }
}
