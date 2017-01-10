package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;
/*
An abstraction that shares the common properties of KmlPoint and GeoJsonPoint
 */
public class Point implements Geometry {

    private final static String GEOMETRY_TYPE = "Point";

    private final LatLng mCoordinates;

    /**
     * Creates a new Point object
     *
     * @param coordinates coordinates of Point to store
     */
    public Point(LatLng coordinates) {
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
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates of the Point
     *
     * @return coordinates of the Point
     */
    public LatLng getGeometryObject() {
        return mCoordinates;
    }

    /**
     * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
     * specification.
     *
     * @return type of geometry
     */
    public String getType() {
        return getGeometryType();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }

}

