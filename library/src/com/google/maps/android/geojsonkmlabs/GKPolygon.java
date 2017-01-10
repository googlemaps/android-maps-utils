package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/*
 * An interface that allows the common properties of GeoJsonPolygons
 * and KmlPolygons to be abstracted
 */
public interface GKPolygon<T> extends Geometry{

    /**
     * Gets an array of outer boundary coordinates
     *
     * @return array of outer boundary coordinates
     */
    public ArrayList<LatLng> getOuterBoundaryCoordinates();

    /**
     * Gets an array of arrays of inner boundary coordinates
     *
     * @return array of arrays of inner boundary coordinates
     */
    public ArrayList<ArrayList<LatLng>> getInnerBoundaryCoordinates();

}
