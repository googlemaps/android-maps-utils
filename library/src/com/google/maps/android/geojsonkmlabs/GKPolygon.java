package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by suvercha on 12/22/16.
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
